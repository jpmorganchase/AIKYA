from flwr.server.strategy.fedadam import FedAdam

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedAdamStrategy(FlowerStrategy):
    """
    Wrapper class for the FedAdam strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedAdam()

