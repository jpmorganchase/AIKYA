from flwr.server.strategy.fedavg_android import FedAvgAndroid

from src.ml_models.model_builder import weighted_metrics_average
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedAvgAndroidStrategy(FlowerStrategy):
    """
    Wrapper class for the FedAvgAndroid strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedAvgAndroid()

