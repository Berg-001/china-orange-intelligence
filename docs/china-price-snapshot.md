# Snapshot verificado do mercado chinês

Data da pesquisa: 2026-08-06. Valores abaixo permanecem separados por estágio da cadeia.

| Referência | Praça/escopo | Categoria | Produto | Publicação original | Normalizado | Fonte |
|---|---|---|---|---:|---:|---|
| 2026-07-14 | Yunfu | varejo monitorado | laranja doméstica | ¥4,50/500 g | ¥9,00/kg | Governo de Yunfu |
| 2026-07-16 | Yingde | varejo monitorado | laranja de primeira classe | ¥6,00/500 g | ¥12,00/kg | Governo de Yingde |
| 2026-07-07 | Yunfu | varejo monitorado | laranja doméstica | ¥4,45/500 g | ¥8,90/kg | Governo de Yunfu |
| 2026-07-09 | China, agregado | saída de fábrica | FCOJ doméstico | ¥25.411,85/t | ¥25,41185/kg | Xinhua Index / Three Gorges |
| 2026-07-09 | China, agregado | saída de fábrica | NFC doméstico | ¥7.193,19/t | ¥7,19319/kg | Xinhua Index / Three Gorges |
| 2026-06-11 | China, destinos | atacado | navel | ¥7,12/kg | ¥7,12/kg | MOA + Xinhua Index |
| 2026-06-11 | Hubei/Zigui | origem | navel | ¥2,22/kg | ¥2,22/kg | Citrus Industry Brain / Xinhua |
| 2026-06-11 | Chongqing/Fengjie | origem | navel | ¥3,40/kg | ¥3,40/kg | Citrus Industry Brain / Xinhua |
| 2026-05-11 | Nantong | atacado | laranja Gannan | ¥2,50/500 g | ¥5,00/kg | DRC Nantong |
| 2026-05-11 | Nantong | atacado | laranja sul-africana | ¥3,50/500 g | ¥7,00/kg | DRC Nantong |
| 2026-05-11 | Nantong | atacado | laranja australiana | ¥9,00/500 g | ¥18,00/kg | DRC Nantong |

## Leitura correta

- O valor de varejo de Yunfu não é comparável diretamente com atacado ou origem.
- FCOJ e NFC têm concentração e rendimento diferentes; preço por kg não torna os dois produtos equivalentes.
- Os valores Xinhua de suco são agregados domésticos de saída de fábrica. O contrato ICE citado nos mesmos relatórios é internacional e fica fora da OMIP China-only.
- Índices em pontos são armazenáveis em uma futura entidade `MarketIndex`, nunca como `MarketPrice`.
- Boletins que proíbem reprodução só podem ser ativados após autorização; o parser existe para testes e futura integração licenciada.
- Yingde foi mantido como candidato: a publicação é oficial, mas o portal apresentou timeout e a planilha XLS precisa de homologação antes de automação.
