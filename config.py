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
    "collected_at", "reference_date", "country_code", "market_name",
    "product", "product_original", "price_min", "price_average",
    "price_max", "currency", "unit", "source_name", "source_url", "status"
]
for directory in (RAW_DIR, PROCESSED_DIR, OUTPUT_DIR, LOG_DIR):
    directory.mkdir(parents=True, exist_ok=True)
