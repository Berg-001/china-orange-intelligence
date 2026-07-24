from pathlib import Path
import pandas as pd
from config import CSV_COLUMNS, CSV_PATH
from models import OrangePriceRecord

def append_records(records: list[OrangePriceRecord], csv_path: Path = CSV_PATH) -> int:
    if not records: return 0
    rows = []
    for record in records:
        row = record.model_dump(mode="json")
        row["source_url"] = str(record.source_url)
        rows.append(row)
    new_df = pd.DataFrame(rows, columns=CSV_COLUMNS)
    if csv_path.exists() and csv_path.stat().st_size > 0:
        current = pd.read_csv(csv_path)
        result = pd.concat([current, new_df], ignore_index=True)
        result = result.drop_duplicates(
            subset=["reference_date","market_name","product","price_average","source_name"],
            keep="last",
        )
    else:
        result = new_df
    result.to_csv(csv_path, index=False)
    return len(new_df)
