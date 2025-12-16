from flwr.server.strategy.krum import Krum

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class KrumStrategy(FlowerStrategy):
    """
    Wrapper class for the Krum strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = Krum(fit_metrics_aggregation_fn=weighted_metrics_average)

