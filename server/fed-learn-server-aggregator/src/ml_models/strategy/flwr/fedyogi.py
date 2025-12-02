from flwr.server.strategy.fedyogi import FedYogi

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedYogiStrategy(FlowerStrategy):
    """
    Wrapper class for the FedYogi strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedYogi()

