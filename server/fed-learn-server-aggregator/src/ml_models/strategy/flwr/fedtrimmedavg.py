from flwr.server.strategy.fedtrimmedavg import FedTrimmedAvg

from src.ml_models.model_builder import weighted_metrics_average
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedTrimmedAvgStrategy(FlowerStrategy):
    """
    Wrapper class for the FedTrimmedAvg strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedTrimmedAvg(fit_metrics_aggregation_fn=weighted_metrics_average)

