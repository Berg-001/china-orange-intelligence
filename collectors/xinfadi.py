from collectors.base import Collector
from models import OrangePriceRecord

class XinfadiCollector(Collector):
    """Placeholder seguro para a fonte Xinfadi.

    Antes de implementar, valide endpoint/página oficial, termos de uso,
    estrutura atual, unidade, significado das colunas e produto exato.
    """
    def collect(self) -> list[OrangePriceRecord]:
        return []
