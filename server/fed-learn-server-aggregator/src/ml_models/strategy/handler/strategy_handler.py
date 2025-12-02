from typing import Tuple, Dict, Any

from src.entities.strategy_run_request import FedLearnServerRequest
from src.ml_models.strategy.handler.flwr_strategy_handler import FlwrStrategyHandler
from src.ml_models.strategy.handler.other_strategy_handler import OtherStrategyHandler
from src.util.constants import VendorTypes
from src.util import log
logger = log.init_logger()

class StrategyHandler:
    def __init__(self, request: FedLearnServerRequest):
        self.request = request

    def aggregate_fit(self) -> Tuple[Any, Dict[str, Any]]:
        if self.request.vendor.lower() == VendorTypes.FLWR:
            handler = FlwrStrategyHandler(self.request)
            logger.info("FlwrStrategyHandler")
        elif self.request.vendor.lower() == VendorTypes.OTHER:
            handler = OtherStrategyHandler(self.request)
            logger.info("OtherStrategyHandler")
        else:
            raise ValueError(f"Unsupported vendor: {self.request.vendor}")
        return handler.aggregate_fit()

    def aggregate_evaluate(self) -> Tuple[Any, Dict[str, Any]]:
        if self.request.vendor.lower() == VendorTypes.FLWR:
            handler = FlwrStrategyHandler(self.request)
        elif self.request.vendor.lower() == VendorTypes.OTHER:
            handler = OtherStrategyHandler(self.request)
        else:
            raise ValueError(f"Unsupported vendor: {self.request.vendor}")
        return handler.aggregate_evaluate()
