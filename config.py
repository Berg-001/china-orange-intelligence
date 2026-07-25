import os
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent

DATA_DIR = BASE_DIR / "data"
RAW_DIR = DATA_DIR / "raw"
PROCESSED_DIR = DATA_DIR / "processed"
OUTPUT_DIR = BASE_DIR / "output"
LOG_DIR = BASE_DIR / "logs"

CSV_PATH = PROCESSED_DIR / "orange_prices.csv"
METRICS_PATH = OUTPUT_DIR / "metrics.json"
HTML_PATH = OUTPUT_DIR / "index.html"

CSV_COLUMNS = [
    "collected_at",
    "reference_date",
    "country_code",
    "market_name",
    "product",
    "product_original",
    "price_min",
    "price_average",
    "price_max",
    "currency",
    "unit",
    "source_name",
    "source_url",
    "status",
]

# Referência comercial da Tridge.
# O valor pode ser sobrescrito por variáveis de ambiente.
TRIDGE_PRICE_USD_KG = float(
    os.getenv("TRIDGE_PRICE_USD_KG", "0.91")
)

ORANGE_BOX_WEIGHT_KG = float(
    os.getenv("ORANGE_BOX_WEIGHT_KG", "40.8")
)

TRIDGE_REFERENCE_YEAR = int(
    os.getenv("TRIDGE_REFERENCE_YEAR", "2026")
)

TRIDGE_SOURCE_NAME = "Tridge"

TRIDGE_SOURCE_URL = (
    "https://www.tridge.com/market-overview/fresh-orange/CN"
)

for directory in (
    RAW_DIR,
    PROCESSED_DIR,
    OUTPUT_DIR,
    LOG_DIR,
):
    directory.mkdir(parents=True, exist_ok=True)