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
