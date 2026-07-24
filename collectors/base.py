from abc import ABC, abstractmethod
from models import OrangePriceRecord
class Collector(ABC):
    @abstractmethod
    def collect(self) -> list[OrangePriceRecord]:
        raise NotImplementedError
