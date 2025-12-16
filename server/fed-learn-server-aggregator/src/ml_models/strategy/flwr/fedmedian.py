from flwr.server.strategy.fedmedian import FedMedian

from src.ml_models.model_builder import weighted_metrics_average
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedMedianStrategy(FlowerStrategy):
    """
    Wrapper class for the FedMedian strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedMedian(fit_metrics_aggregation_fn=weighted_metrics_average)

