"""Collect auditable China-market observations into repository JSON/CSV files."""
from __future__ import annotations

import csv
import hashlib
import json
import re
import sys
from datetime import date, datetime, timezone
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path
from urllib.parse import urljoin
from xml.etree import ElementTree

import requests
from bs4 import BeautifulSoup

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "config" / "static-sources.json"
DATA_DIR = ROOT / "frontend" / "public" / "data"
JSON_PATH = DATA_DIR / "prices.json"
CSV_PATH = DATA_DIR / "prices.csv"
CEPEA_JSON_PATH = DATA_DIR / "cepea-citrus.json"
CEPEA_CSV_PATH = DATA_DIR / "cepea-citrus.csv"
BENCHMARKS_PATH = DATA_DIR / "benchmarks.json"
HF_CITRUS_URL = "https://www.hfbrasil.org.br/br/estatistica/citros.aspx"
USER_AGENT = "OMIP/0.1 (+https://github.com/Berg-001/china-orange-intelligence)"
TIMEOUT = 30
ARTICLE_LINK = re.compile(r"/jgzs/wenzixiangqingye/detail/\d{8}/[^\"'#?]+\.html")
FULL_DATE = re.compile(r"(20\d{2})年(\d{1,2})月(\d{1,2})日")
PRICES = {
    "FCOJ": re.compile(r"国内冷冻浓缩橙汁出厂价(?:为|报)?\s*([0-9]+(?:\.[0-9]+)?)\s*元[/／]吨"),
    "NFC": re.compile(r"国内非浓缩还原橙汁出厂价(?:为|报)?\s*([0-9]+(?:\.[0-9]+)?)\s*元[/／]吨"),
}
FIELDS = [
    "id", "country", "city", "market", "product", "productNameOriginal", "specification",
    "category", "originalPrice", "originalCurrency", "originalUnit", "pricePerKg", "priceUsd",
    "priceBrl", "priceCny", "fxSource", "fxReferenceDate", "source", "url", "confidenceScore",
    "referenceDate", "collectionTime", "consensus",
]


def get(session: requests.Session, url: str) -> str:
    response = session.get(url, timeout=TIMEOUT)
    response.raise_for_status()
    response.encoding = response.apparent_encoding or "utf-8"
    return response.text


def ecb_rates(session: requests.Session) -> dict[date, dict[str, Decimal]]:
    url = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml"
    response = session.get(url, timeout=TIMEOUT)
    response.raise_for_status()
    root = ElementTree.fromstring(response.content)
    result: dict[date, dict[str, Decimal]] = {}
    for node in root.iter():
        day = node.attrib.get("time")
        if not day:
            continue
        rates = {child.attrib["currency"]: Decimal(child.attrib["rate"]) for child in node if "currency" in child.attrib}
        rates["EUR"] = Decimal(1)
        result[date.fromisoformat(day)] = rates
    if not result:
        raise RuntimeError("ECB returned no exchange rates")
    return result


def applicable_rates(all_rates: dict[date, dict[str, Decimal]], reference: date) -> tuple[date, dict[str, Decimal]]:
    eligible = [day for day in all_rates if day <= reference]
    chosen = max(eligible) if eligible else min(all_rates)
    return chosen, all_rates[chosen]


def decimal(value: Decimal) -> float:
    return float(value.quantize(Decimal("0.000001"), rounding=ROUND_HALF_UP))


def observation_id(parts: list[str]) -> str:
    return hashlib.sha256("|".join(parts).encode("utf-8")).hexdigest()[:32]


def parse_xinhua(html: str, url: str, source: dict, rates_by_day: dict[date, dict[str, Decimal]]) -> list[dict]:
    text = BeautifulSoup(html, "html.parser").get_text(" ", strip=True)
    match_date = FULL_DATE.search(text)
    if not match_date:
        return []
    reference = date(*map(int, match_date.groups()))
    fx_date, rates = applicable_rates(rates_by_day, reference)
    if not {"USD", "BRL", "CNY"}.issubset(rates):
        raise RuntimeError(f"ECB rates missing currency for {fx_date}")
    collected = datetime.now(timezone.utc).isoformat()
    result = []
    originals = {"FCOJ": "国内冷冻浓缩橙汁", "NFC": "国内非浓缩还原橙汁"}
    for product, pattern in PRICES.items():
        price_match = pattern.search(text)
        if not price_match:
            continue
        original = Decimal(price_match.group(1))
        cny_kg = original / Decimal(1000)
        usd_kg = cny_kg * rates["USD"] / rates["CNY"]
        brl_kg = cny_kg * rates["BRL"] / rates["CNY"]
        key = ["CN", product, "INDUSTRY", reference.isoformat(), url, str(original)]
        result.append({
            "id": observation_id(key), "country": "CN", "city": None,
            "market": "China national factory-gate aggregate", "product": product,
            "productNameOriginal": originals[product], "specification": "Domestic factory-gate aggregate",
            "category": "INDUSTRY", "originalPrice": float(original), "originalCurrency": "CNY",
            "originalUnit": "tonne", "pricePerKg": decimal(cny_kg), "priceUsd": decimal(usd_kg),
            "priceBrl": decimal(brl_kg), "priceCny": decimal(cny_kg), "fxSource": "ECB_EXR_REFERENCE",
            "fxReferenceDate": fx_date.isoformat(), "source": source["name"], "url": url,
            "confidenceScore": int(source["confidenceScore"]), "referenceDate": reference.isoformat(),
            "collectionTime": collected, "consensus": False,
        })
    return result


def discover_urls(session: requests.Session, source: dict) -> list[str]:
    urls = list(source.get("seedUrls", []))
    listing = source.get("listingUrl")
    if listing:
        html = get(session, listing)
        urls.extend(urljoin(listing, path) for path in ARTICLE_LINK.findall(html))
    return list(dict.fromkeys(urls))[: int(source.get("maxArticles", 20))]


def hf_reference_date(day_month: str, today: date) -> date:
    day, month = map(int, day_month.split("/"))
    candidate = date(today.year, month, day)
    if candidate > today:
        candidate = date(today.year - 1, month, day)
    return candidate


def collect_hf_cepea(session: requests.Session) -> int:
    """Append official HF Brasil/CEPEA regional pear-orange observations."""
    soup = BeautifulSoup(get(session, HF_CITRUS_URL), "html.parser")
    table = soup.find("table")
    if table is None:
        raise RuntimeError("HF Brasil citrus price table not found")
    rows = table.find_all("tr")
    headers = [cell.get_text(" ", strip=True) for cell in rows[0].find_all(["th", "td"])]
    if len(headers) < 4 or headers[:3] != ["Produto", "Região", "Unidade"]:
        raise RuntimeError("HF Brasil citrus table contract changed")
    today = datetime.now(timezone.utc).date()
    incoming = []
    for tr in rows[1:]:
        cells = [cell.get_text(" ", strip=True) for cell in tr.find_all(["th", "td"])]
        if len(cells) != len(headers) or not cells[0].startswith("Laranja Pêra") or "40,8" not in cells[2]:
            continue
        region = cells[1].replace(" (região)", "")
        for heading, raw in zip(headers[3:], cells[3:]):
            if not re.fullmatch(r"\d{2}/[a-zç]{3}", heading.lower()) or not re.fullmatch(r"\d+[,.]\d{2}", raw):
                continue
            months = {"jan": 1, "fev": 2, "mar": 3, "abr": 4, "mai": 5, "jun": 6,
                      "jul": 7, "ago": 8, "set": 9, "out": 10, "nov": 11, "dez": 12}
            day_text, month_text = heading.lower().split("/")
            reference = date(today.year, months[month_text], int(day_text))
            if reference > today:
                reference = date(today.year - 1, months[month_text], int(day_text))
            value = float(raw.replace(".", "").replace(",", "."))
            incoming.append({"product": "FRESH_ORANGE", "date": reference.isoformat(),
                             "price": value, "region": region, "marketLevel": "Na árvore",
                             "source": "HORTIFRUTI/CEPEA"})
    if not incoming:
        raise RuntimeError("HF Brasil returned no compatible pear-orange observations")
    payload = json.loads(CEPEA_JSON_PATH.read_text(encoding="utf-8")) if CEPEA_JSON_PATH.exists() else {}
    existing = payload.get("content", [])
    keys = {(row.get("product"), row.get("date"), row.get("region"), row.get("source")) for row in existing}
    added = [row for row in incoming if (row["product"], row["date"], row["region"], row["source"]) not in keys]
    if not added:
        return 0
    content = existing + added
    content.sort(key=lambda row: (row["date"], row["product"], row.get("region", "")), reverse=True)
    payload.update({"source": "CEPEA/ESALQ-USP and HORTIFRUTI/CEPEA", "url": HF_CITRUS_URL,
                    "license": "CC BY-NC 4.0", "licenseUrl": "https://creativecommons.org/licenses/by-nc/4.0/",
                    "currency": "BRL", "unit": "box_40_8_kg", "content": content})
    CEPEA_JSON_PATH.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    fields = ["product", "date", "price", "region", "marketLevel", "dailyChangePercent",
              "monthlyChangePercent", "fiveDayAverage", "source"]
    with CEPEA_CSV_PATH.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(content)
    latest = max(row["date"] for row in incoming)
    latest_rows = [row for row in incoming if row["date"] == latest]
    average = sum(Decimal(str(row["price"])) for row in latest_rows) / Decimal(len(latest_rows))
    benchmarks = json.loads(BENCHMARKS_PATH.read_text(encoding="utf-8"))
    brazil = next(row for row in benchmarks["content"] if row["country"] == "BR")
    brazil.update({"product": "Laranja pera in natura", "marketLevel": "Média simples de 4 regiões, na árvore",
                   "referencePeriod": latest, "pricePerKg": decimal(average / Decimal("40.8")),
                   "priceBoxOriginal": decimal(average), "priceBoxBrl": decimal(average),
                   "source": "HORTIFRUTI/CEPEA", "url": HF_CITRUS_URL})
    brazil.pop("dailyChangePercent", None)
    brazil.pop("fiveDayAverage", None)
    benchmarks["generatedAt"] = datetime.now(timezone.utc).isoformat()
    BENCHMARKS_PATH.write_text(json.dumps(benchmarks, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return len(added)


def load_existing() -> list[dict]:
    if not JSON_PATH.exists():
        return []
    payload = json.loads(JSON_PATH.read_text(encoding="utf-8"))
    return payload.get("content", payload if isinstance(payload, list) else [])


def save(records: list[dict]) -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    records.sort(key=lambda row: (row["referenceDate"], row["product"], row["market"]), reverse=True)
    payload = {"generatedAt": datetime.now(timezone.utc).isoformat(), "content": records, "totalElements": len(records)}
    JSON_PATH.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    with CSV_PATH.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=FIELDS)
        writer.writeheader()
        writer.writerows({field: row.get(field) for field in FIELDS} for row in records)


def main() -> int:
    config = json.loads(CONFIG.read_text(encoding="utf-8"))
    session = requests.Session()
    session.headers.update({"User-Agent": USER_AGENT, "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.5"})
    rates = ecb_rates(session)
    existing = {row["id"]: row for row in load_existing()}
    previous_ids = set(existing)
    errors = []
    try:
        hf_added = collect_hf_cepea(session)
        print(f"Added {hf_added} HF Brasil/CEPEA regional observations")
    except Exception as exc:
        errors.append(f"HORTIFRUTI/CEPEA: {exc}")
    for source in config["sources"]:
        if not source.get("enabled"):
            continue
        try:
            for url in discover_urls(session, source):
                try:
                    for row in parse_xinhua(get(session, url), url, source, rates):
                        existing.setdefault(row["id"], row)
                except Exception as exc:  # one article must not stop the source
                    errors.append(f"{url}: {exc}")
        except Exception as exc:
            errors.append(f"{source['name']}: {exc}")
    data_files_exist = JSON_PATH.exists() and CSV_PATH.exists()
    if set(existing) != previous_ids or not data_files_exist:
        save(list(existing.values()))
        status = f"Saved {len(existing)} unique China-market observations"
    else:
        status = f"No new observations; preserved {len(existing)} existing records"
    for error in errors:
        print(f"WARNING {error}", file=sys.stderr)
    print(status)
    return 0 if existing else 2


if __name__ == "__main__":
    raise SystemExit(main())
