from flwr.server.strategy.fedxgb_nn_avg import FedXgbNnAvg

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedXgbNnAvgStrategy(FlowerStrategy):
    """
    Wrapper class for the FedXgbNnAvg strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedXgbNnAvg()

