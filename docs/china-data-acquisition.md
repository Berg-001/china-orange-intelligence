# Aquisição de dados exclusivamente do mercado chinês

## Regra de inclusão

Uma observação só pode entrar em `MarketPrice` quando a praça, o mercado, o ponto de venda ou a transação estiver localizado na China (`country=CN`). A origem física da fruta não altera essa regra: uma laranja sul-africana negociada em Nantong é preço do mercado chinês; uma laranja chinesa negociada fora da China não é.

## Plataformas recomendadas

1. **Ministério da Agricultura — 重点农产品市场信息平台 / 全国农产品批发市场价格信息系统**. Primeira escolha gratuita. Solicitar acesso ou documentação oficial para consulta automatizada antes de integrar chamadas internas do portal.
2. **Beijing Xinfadi (北京新发地)**. Publica preço mínimo, médio, máximo, especificação, origem, unidade e data. Contatar a equipe indicada no próprio portal para licença de redistribuição e acesso estruturado.
3. **Guangzhou Jiangnan (广州江南果菜批发市场)**. Mercado relevante para frutas nacionais e importadas; solicitar feed diário, dicionário de produtos e histórico.
4. **卓创资讯 / SCI99**. Melhor candidato comercial para dados industriais e cadeia de sucos. Pedir demonstração específica para `鲜橙`, `工业橙`, `冷冻浓缩橙汁/FCOJ` e `非浓缩还原橙汁/NFC`, além de proposta de API e direitos de armazenamento.
5. **聚美智数 / Jumdata**. API pronta e precificação pública por chamadas. Usar apenas após confirmar que devolve laranja por mercado, unidade, data e fonte primária; o teste gratuito de 10 chamadas deve ser usado antes de contratar.
6. **CnOpenData**. Útil para backfill acadêmico, não para operação diária: a página informa atualização anual e parte das séries termina em 2023/2025.

## Entrada legítima em canais WeChat

Não comprar convites de terceiros e não coletar grupos por QR codes republicados. O processo recomendado é:

1. Seguir as contas oficiais `中国果品流通协会`, Beijing Xinfadi e Guangzhou Jiangnan.
2. Contatar a Associação Chinesa de Circulação de Frutas pelo formulário oficial, informando empresa, objetivo e mercados de interesse.
3. Solicitar ao atendimento oficial de cada mercado apresentação ao gerente da categoria cítricos ou ao grupo de comerciantes credenciados.
4. Pedir autorização escrita para usar mensagens como fonte e confirmar se valores são oferta, pedido, negócio fechado ou média do mercado.
5. Manter nome do informante/administrador apenas em cadastro protegido; nunca expor telefone ou WeChat ID na API pública.
6. Exigir pelo menos mercado, produto/qualidade, origem, preço, unidade e timestamp em toda mensagem aproveitada.

### Mensagem inicial em chinês

> 您好，我们正在开发一个专注于中国市场的橙子及橙汁价格监测平台。我们只使用经过授权、可追溯的数据。请问贵单位是否提供鲜橙、加工橙、冷冻浓缩橙汁（FCOJ）或非浓缩还原橙汁（NFC）的每日价格数据、API、付费订阅或行业微信群？我们希望了解数据字段、更新频率、历史数据、授权范围及报价。谢谢！

Tradução: estamos desenvolvendo uma plataforma de monitoramento de preços de laranja e suco focada exclusivamente na China; buscamos dados diários, API, assinatura ou grupo setorial autorizado, incluindo campos, frequência, histórico, licença e preço.

## Checklist antes de assinar

- O preço representa oferta, negócio efetivo, média ou índice?
- Mercado e cidade estão presentes em cada registro?
- Há distinção entre fruta fresca, industrial, FCOJ e NFC?
- Unidade, especificação/Brix, embalagem e impostos estão documentados?
- Existe histórico e política de revisão?
- A licença permite armazenar, converter moedas, criar gráficos e expor dados pela API?
- Há API ou exportação CSV, SLA e aviso de mudança de schema?
- O fornecedor entrega amostra de 30 dias para comparação com fontes oficiais?

Não contratar se a fonte não permitir auditoria até o publicador original ou se proibir o armazenamento necessário à OMIP.
