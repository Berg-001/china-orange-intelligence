import json
from pathlib import Path
from jinja2 import Environment, FileSystemLoader, select_autoescape
from config import BASE_DIR, HTML_PATH, METRICS_PATH

def save_metrics(metrics: dict, path: Path = METRICS_PATH):
    path.write_text(json.dumps(metrics, ensure_ascii=False, indent=2), encoding="utf-8")

def generate_html(metrics: dict, path: Path = HTML_PATH):
    env = Environment(loader=FileSystemLoader(BASE_DIR / "templates"), autoescape=select_autoescape(["html","xml"]))
    path.write_text(env.get_template("dashboard.html").render(metrics=metrics), encoding="utf-8")
