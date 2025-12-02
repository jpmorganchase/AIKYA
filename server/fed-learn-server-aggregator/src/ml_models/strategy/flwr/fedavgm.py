from flwr.server.strategy.fedavgm import FedAvgM

from src.ml_models.model_builder import weighted_metrics_average
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedAvgMStrategy(FlowerStrategy):
    """
    Wrapper class for the FedAvgM strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedAvgM(fit_metrics_aggregation_fn=weighted_metrics_average)