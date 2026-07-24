from datetime import date, datetime, timezone
from decimal import Decimal
from collectors.base import Collector
from models import OrangePriceRecord

class SampleCollector(Collector):
    """Dados simulados para validar a arquitetura antes do coletor real."""
    def collect(self) -> list[OrangePriceRecord]:
        return [OrangePriceRecord(
            collected_at=datetime.now(timezone.utc),
            reference_date=date.today(),
            market_name="Beijing Xinfadi Wholesale Market",
            product_original="Navel Orange",
            price_min=Decimal("7.20"),
            price_average=Decimal("8.10"),
            price_max=Decimal("9.00"),
            source_name="POC manual",
            source_url="https://www.xinfadi.com.cn/",
        )]
