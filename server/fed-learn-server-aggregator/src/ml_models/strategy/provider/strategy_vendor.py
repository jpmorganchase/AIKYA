from abc import ABC

"""
  Abstract base class representing a strategy vendor. 
"""


class StrategyVendor(ABC):
    """
    Abstract base class for StrategyVendor.
    This class can be extended to implement specific strategies.
    """
    def create_strategy(self, strategy_type: str):
        pass
