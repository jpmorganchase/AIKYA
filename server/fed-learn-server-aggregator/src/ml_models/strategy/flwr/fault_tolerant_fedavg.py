from flwr.server.strategy.fault_tolerant_fedavg import FaultTolerantFedAvg

from src.ml_models.model_builder import weighted_metrics_average
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FaultTolerantFedAvgStrategy(FlowerStrategy):
    """
    Wrapper class for the FaultTolerantFedAvg strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FaultTolerantFedAvg(fit_metrics_aggregation_fn=weighted_metrics_average)

