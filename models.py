from datetime import date, datetime
from decimal import Decimal
from pydantic import BaseModel, Field, HttpUrl

class OrangePriceRecord(BaseModel):
    collected_at: datetime
    reference_date: date
    country_code: str = Field(default="CN", min_length=2, max_length=2)
    market_name: str
    product: str = "navel_orange"
    product_original: str
    price_min: Decimal = Field(ge=0)
    price_average: Decimal = Field(ge=0)
    price_max: Decimal = Field(ge=0)
    currency: str = "CNY"
    unit: str = "kg"
    source_name: str
    source_url: HttpUrl
    status: str = "valid"
