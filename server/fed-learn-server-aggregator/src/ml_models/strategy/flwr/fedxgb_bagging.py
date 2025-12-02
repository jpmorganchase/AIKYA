from flwr.server.strategy.fedxgb_bagging import FedXgbBagging

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedXgbBaggingStrategy(FlowerStrategy):
    """
    Wrapper class for the FedXgbBagging strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedXgbBagging()

