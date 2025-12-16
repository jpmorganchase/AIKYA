from flwr.server.strategy.fedprox import FedProx

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedProxStrategy(FlowerStrategy):
    """
    Wrapper class for the FedProx strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedProx()

