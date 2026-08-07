# Política e catálogo de fontes

Nenhuma fonte real está ativa nesta versão. Isso é intencional: uma cotação só entra na plataforma após validação do contrato e da semântica do dado.

| Prioridade | Candidato | Uso pretendido | Estado |
|---|---|---|---|
| 1 | Ministério da Agricultura da China | preços oficiais e contexto agrícola | validar endpoint e licença |
| 1 | Plataforma de Informações de Mercado do Ministério da Agricultura | transações e preços em CNY/kg | conteúdo confirmado; API ainda não homologada |
| 1 | Comissão de Desenvolvimento e Reforma de Nantong | laranja fresca atacadista e varejista | parser de boletim implementado, ativação opt-in |
| 1 | Governo de Yunfu | laranja doméstica no varejo monitorado | parser implementado; requer autorização para reprodução |
| 1 | Governo de Yingde | laranja de primeira classe no varejo diário | candidato; portal/XLS ainda instável |
| 1 | Xinhua Index / Chongqing Three Gorges Citrus Trading Center | FCOJ e NFC domésticos na saída de fábrica | parser monetário implementado, ativação opt-in |
| 2 | USDA Market News | comparação EUA/exportação | mapear datasets oficiais |
| 2 | FAOSTAT | contexto e séries agregadas | não confundir com preço diário |
| 2 | CEPEA/ESALQ | comparação Brasil | validar direitos de redistribuição |
| 3 | ICE Futures U.S. | FCOJ futuro | validar licença; futuro não é preço físico |

## Checklist de ativação

1. URL e publicador oficiais confirmados.
2. Permissão de acesso automatizado e redistribuição confirmada.
3. Produto, qualidade, praça, moeda e unidade sem ambiguidade.
4. Data de referência diferenciada da data de coleta.
5. Parser coberto por fixture e teste de contrato.
6. Falhas não geram zeros nem reaproveitam silenciosamente valor antigo.
7. Mudanças de schema geram alerta e quarentena.

## Evidências verificadas em 2026

- Nantong, 27/04 e 11/05: laranja Gannan a ¥2,50/500 g, sul-africana a ¥3,50/500 g e australiana a ¥9/500 g — equivalentes a ¥5, ¥7 e ¥18/kg.
- Plataforma do Ministério da Agricultura: transação de laranja navel (`脐橙`) a ¥6/kg, origem Yichang/Hubei e destino Wuxi/Jiangsu, em 21/07.
- Chongqing, janeiro: médias de origem de ¥2,11/kg para navel de Fengjie/Wushan e de ¥1,25/kg para Jin orange de Kaizhou.
- Índices industriais Xinhua em 12/02: FCOJ 1056,81; NFC 1153,73; índice geral de suco 1093,98. São índices, não preços monetários, e não entram em `MarketPrice`.

Essas observações não devem ser misturadas: preço de origem, atacado, transação eletrônica e índice industrial representam estágios e metodologias diferentes.
