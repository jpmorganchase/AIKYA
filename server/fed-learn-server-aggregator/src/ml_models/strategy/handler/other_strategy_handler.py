from typing import Tuple, Dict, Any

from src.entities.strategy_run_request import FedLearnServerRequest


class OtherStrategyHandler:
    """
    Handler for Other strategy.
    """

    def __init__(self, request: FedLearnServerRequest):
        self.request = request

    def aggregate_fit(self) -> Tuple[Any, Dict[str, Any]]:
        # Implement the aggregation logic for Other strategy(ies)
        pass

    def aggregate_evaluate(self) -> Tuple[Any, Dict[str, Any]]:
        # Implement the evaluation aggregation logic for Other strategy(ies)
        pass
