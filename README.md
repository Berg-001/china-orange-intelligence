# China Orange Intelligence

POC em Python + n8n para coleta diária, histórico CSV, estatísticas e dashboard HTML.

## Fluxo

```text
n8n (08:00) -> POST /run -> Python -> CSV -> metrics.json -> index.html -> Nginx
```

## Subir o projeto

```bash
cp .env.example .env
docker compose up --build -d
```

Acessos:

- API: http://localhost:8000/docs
- n8n: http://localhost:5678
- Dashboard: http://localhost:8080

## Testar manualmente

```bash
curl -X POST "http://localhost:8000/run?use_sample=true"   -H "X-API-Key: change_me_pipeline_key"
```

## Workflow n8n

Importe `workflows/china-orange-daily.json`, teste e depois ative.

## Estado atual

O `SampleCollector` valida o fluxo completo. O `XinfadiCollector` está vazio de propósito: o scraping real deve ser implementado após validar endpoint, termos de uso, estrutura, unidade e produto exato. MOA China e Trading Economics permanecem como contexto agrícola e macro, sem serem confundidos com preço atacadista.
