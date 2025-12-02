from src.ml_models.strategy.provider.flwr_strategy_vendor import FlwrStrategyVendor
from src.ml_models.strategy.provider.other_strategy_vendor import OtherStrategyVendor
from src.ml_models.strategy.provider.strategy_vendor import StrategyVendor
from src.util.constants import VendorTypes


class StrategyProvider:
    @staticmethod
    def get_strategy_vendor(vendor: str) -> StrategyVendor:
        """
        Static method to create a FlowerStrategy instance based on the strategy type.

        :param vendor: str, the type of strategy vendor to create ('flwr', 'other')
        :return: FlowerStrategy, an instance of a concrete FlowerStrategy
        :raises ValueError: if the strategy type is unknown
        """
        if vendor == VendorTypes.FLWR:
            return FlwrStrategyVendor()
        elif vendor == VendorTypes.OTHER:
            return OtherStrategyVendor()
        else:
            raise ValueError(f"Unknown vendor strategy type: {vendor}")