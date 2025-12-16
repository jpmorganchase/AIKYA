from flwr.server.strategy.fedopt import FedOpt

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedOptStrategy(FlowerStrategy):
    """
    Wrapper class for the FedOpt strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedOpt()

