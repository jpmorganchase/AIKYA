from flwr.server.strategy.fedavg import FedAvg

from src.ml_models.model_builder import weighted_metrics_average
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedAvgStrategy(FlowerStrategy):
    """
    Wrapper class for the FedAvg strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        # self.strategy = FedAvg(
        #     fraction_fit=0.2,
        #     fit_metrics_aggregation_fn=weighted_metrics_average
        # )
        self.strategy = FedAvg()
