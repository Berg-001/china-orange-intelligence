import os
from fastapi import FastAPI, Header, HTTPException
from pipeline import run_pipeline
app = FastAPI(title="China Orange Intelligence Pipeline", version="0.1.0")
API_KEY = os.getenv("PIPELINE_API_KEY", "change_me_pipeline_key")
@app.get("/health")
def health(): return {"status":"UP"}
@app.post("/run")
def run(x_api_key: str|None=Header(default=None), use_sample: bool=True):
    if x_api_key != API_KEY: raise HTTPException(status_code=401, detail="Invalid API key")
    return run_pipeline(use_sample)
