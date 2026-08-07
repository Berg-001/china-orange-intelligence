# Orange Market Intelligence Platform (OMIP)

POC para coleta, normalização e análise histórica de preços de laranja e suco exclusivamente no mercado chinês. O projeto privilegia rastreabilidade: registros nunca são sobrescritos, toda cotação preserva fonte, valor original e confiança, e dados demonstrativos recebem confiança `0`.

## Arquitetura

```text
React/MUI -> Spring REST/OpenAPI -> collectors -> normalizer -> consensus -> PostgreSQL
                                      ^                         |
                                      +--- scheduler 07:00 UTC -+
```

- `backend/`: Java 21, Spring Boot, JPA, Flyway, scheduler e OpenAPI.
- `frontend/`: React, TypeScript, Vite, Material UI e Chart.js.
- `collectors/`, `pipeline.py` e demais arquivos Python: POC anterior preservada como referência; não participa do Compose atual.
- Consenso diário: média ponderada pelo `confidence_score`, criado somente com duas ou mais fontes não demonstrativas equivalentes (data, país, produto e categoria).

## Executar

```bash
docker compose up --build
curl -X POST http://localhost:8080/admin/collect
```

Acessos:

- Dashboard: http://localhost:3000
- OpenAPI/Swagger: http://localhost:8080/docs
- Health: http://localhost:8080/health

O Compose habilita um coletor demonstrativo para validar o fluxo. Ele usa domínio `.invalid` e confiança `0`; desabilite em produção com `DEMO_COLLECTOR_ENABLED=false`. O endpoint administrativo também deve ser desabilitado ou protegido: `ADMIN_ENDPOINT_ENABLED=false`.

O coletor de boletins da Comissão de Desenvolvimento e Reforma de Nantong é opt-in. Ele aceita somente linhas de laranja com unidade explícita `元/500克`, converte 500 g para kg e rejeita páginas sem data ou sem linhas compatíveis. Configure URLs oficiais homologadas em `NANTONG_BULLETIN_URLS` e ative `NANTONG_COLLECTOR_ENABLED=true`.

Há também coletores opt-in para varejo oficial de Yunfu e preços domésticos de saída de fábrica FCOJ/NFC publicados pelo Xinhua Index. Eles preservam `productNameOriginal` e `specification`, e não misturam varejo, atacado e indústria no consenso. Consulte `docs/china-price-snapshot.md`.

O consenso exige pelo menos dois publicadores distintos e correspondência exata de data, cidade, mercado, produto, categoria e especificação. Linhas repetidas ou diferentes origens dentro do mesmo boletim não contam como fontes independentes.

## API

- `GET /prices/latest`
- `GET /prices/history?product=FRESH_ORANGE&from=2026-01-01&to=2026-12-31`
- `GET /prices/china`, `/prices/orange`, `/prices/fcoj`, `/prices/nfc`
- `GET /sources`
- `GET /health`
- `POST /admin/collect` (somente quando explicitamente habilitado)

Os preços normalizados estão disponíveis em `priceUsd`, `priceBrl` e `priceCny`, sempre por kg. `originalPrice`, `originalCurrency` e `originalUnit` preservam a publicação da fonte. `fxSource` e `fxReferenceDate` identificam a procedência e a data do câmbio usado. Caixa sem peso explícito usa o padrão documentado de 40,8 kg.

Novos registros com país diferente de `CN` são rejeitados no normalizador e por constraint no PostgreSQL. Registros comparativos legados, se existirem, são preservados mas não aparecem na API.

## Qualidade dos dados

Antes de ativar um coletor real, documente no código e em `docs/sources.md`: proprietário, URL oficial, licença/termos, significado do produto, unidade, periodicidade, timezone, tratamento de revisões e fixtures de contrato. Scraping não deve ser ativado até a estabilidade e permissão de uso serem verificadas.

As taxas cambiais configuráveis são apenas fallback da POC e aparecem explicitamente como `CONFIGURED_FALLBACK`. Uma implantação real deve integrar a API oficial EXR do BCE e falhar de modo seguro quando a taxa aplicável não estiver disponível.

## Desenvolvimento

```bash
cd backend && mvn test
cd frontend && npm install && npm run build
```

Variáveis estão descritas em `.env.example`. Não há autenticação nesta POC; não exponha o endpoint administrativo publicamente.
