from pathlib import Path
from typing import Any
import math
import pandas as pd
from config import CSV_PATH

def _safe_float(value: Any):
    if value is None or pd.isna(value): return None
    value = float(value)
    return value if math.isfinite(value) else None

def calculate_statistics(csv_path: Path = CSV_PATH) -> dict:
    if not csv_path.exists() or csv_path.stat().st_size == 0:
        return {"status":"no_data","message":"Ainda não existem observações no CSV.","sample_size":0}
    df = pd.read_csv(csv_path)
    df["reference_date"] = pd.to_datetime(df["reference_date"], errors="coerce")
    df["price_average"] = pd.to_numeric(df["price_average"], errors="coerce")
    valid = df[(df["status"] == "valid") & df["reference_date"].notna() & df["price_average"].notna()].copy()
    if valid.empty:
        return {"status":"no_valid_data","message":"O CSV não contém observações válidas.","sample_size":0,"invalid_count":int(len(df))}
    valid = valid.sort_values("reference_date")
    s = valid["price_average"]
    q1, q3 = s.quantile(.25), s.quantile(.75)
    iqr = q3-q1
    outliers = valid[(s < q1-1.5*iqr) | (s > q3+1.5*iqr)]
    current = s.iloc[-1]
    previous = s.iloc[-2] if len(s) >= 2 else None
    change = ((current-previous)/previous*100) if previous not in (None,0) else None
    mean = s.mean(); std = s.std(ddof=1) if len(s)>1 else 0.0
    cv = (std/mean*100) if mean else None
    evidence = "stronger_statistical_basis" if len(s)>=15 else "initial_trend" if len(s)>=7 else "preliminary_comparison" if len(s)>=2 else "point_observation"
    window = min(7, len(s))
    latest = valid.iloc[-1]
    return {
        "status":"ok", "product":str(latest["product"]), "market_name":str(latest["market_name"]),
        "currency":str(latest["currency"]), "unit":str(latest["unit"]),
        "reference_date":latest["reference_date"].date().isoformat(), "sample_size":int(len(valid)),
        "invalid_count":int(len(df)-len(valid)), "evidence_level":evidence,
        "current_price":_safe_float(current), "previous_price":_safe_float(previous), "change_pct":_safe_float(change),
        "mean":_safe_float(mean), "median":_safe_float(s.median()), "minimum":_safe_float(s.min()), "maximum":_safe_float(s.max()),
        "standard_deviation":_safe_float(std), "coefficient_variation_pct":_safe_float(cv),
        "q1":_safe_float(q1), "q3":_safe_float(q3), "moving_average":_safe_float(s.rolling(window).mean().iloc[-1]),
        "moving_average_window":window, "outlier_count":int(len(outliers)),
        "source_name":str(latest["source_name"]), "source_url":str(latest["source_url"]),
        "disclaimer":"Dados de demonstração ou coleta automatizada sujeitos a validação. Não utilizar isoladamente para decisões comerciais."
    }
