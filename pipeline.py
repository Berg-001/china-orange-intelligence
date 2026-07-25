import logging
from datetime import datetime, timezone

from analytics.statistics import calculate_statistics
from collectors.sample import SampleCollector
from config import (
    LOG_DIR,
    ORANGE_BOX_WEIGHT_KG,
    TRIDGE_PRICE_USD_KG,
    TRIDGE_REFERENCE_YEAR,
    TRIDGE_SOURCE_NAME,
    TRIDGE_SOURCE_URL,
)
from reports.html_report import generate_html, save_metrics
from storage import append_records


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
    handlers=[
        logging.FileHandler(
            LOG_DIR / "pipeline.log",
            encoding="utf-8",
        ),
        logging.StreamHandler(),
    ],
)

logger = logging.getLogger(__name__)


def build_commercial_reference() -> dict:
    """
    Cria a referência comercial da Tridge e calcula
    o preço equivalente para uma caixa de 40,8 kg.
    """

    box_price_usd = round(
        TRIDGE_PRICE_USD_KG * ORANGE_BOX_WEIGHT_KG,
        2,
    )

    return {
        "source_name": TRIDGE_SOURCE_NAME,
        "source_url": TRIDGE_SOURCE_URL,
        "reference_type": "estimate",
        "reference_year": TRIDGE_REFERENCE_YEAR,
        "product": "Fresh Orange",
        "country_code": "CN",
        "price_usd_kg": TRIDGE_PRICE_USD_KG,
        "box_weight_kg": ORANGE_BOX_WEIGHT_KG,
        "box_price_usd": box_price_usd,
        "currency": "USD",
        "unit": "kg",
    }


def run_pipeline(use_sample: bool = True) -> dict:
    started = datetime.now(timezone.utc)

    collector = SampleCollector() if use_sample else None
    records = collector.collect() if collector else []

    written = append_records(records)

    metrics = calculate_statistics()

    commercial_reference = build_commercial_reference()

    # A referência passa a fazer parte do metrics.json
    # e também é enviada ao template HTML.
    metrics["commercial_reference"] = commercial_reference

    save_metrics(metrics)
    generate_html(metrics)

    finished = datetime.now(timezone.utc)

    result = {
        "status": "success",
        "started_at": started.isoformat(),
        "finished_at": finished.isoformat(),
        "records_received": len(records),
        "records_written": written,
        "metrics_status": metrics.get("status"),
        "commercial_reference": commercial_reference,
    }

    logger.info("Pipeline concluído: %s", result)

    return result


if __name__ == "__main__":
    print(run_pipeline(True))