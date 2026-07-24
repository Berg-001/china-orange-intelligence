import logging
from datetime import datetime, timezone
from analytics.statistics import calculate_statistics
from collectors.sample import SampleCollector
from reports.html_report import generate_html, save_metrics
from storage import append_records
from config import LOG_DIR
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s", handlers=[logging.FileHandler(LOG_DIR/"pipeline.log", encoding="utf-8"), logging.StreamHandler()])
logger = logging.getLogger(__name__)

def run_pipeline(use_sample: bool=True) -> dict:
    started = datetime.now(timezone.utc)
    collector = SampleCollector() if use_sample else None
    records = collector.collect() if collector else []
    written = append_records(records)
    metrics = calculate_statistics(); save_metrics(metrics); generate_html(metrics)
    result = {"status":"success","started_at":started.isoformat(),"finished_at":datetime.now(timezone.utc).isoformat(),"records_received":len(records),"records_written":written,"metrics_status":metrics.get("status")}
    logger.info("Pipeline concluído: %s", result)
    return result
if __name__ == "__main__": print(run_pipeline(True))
