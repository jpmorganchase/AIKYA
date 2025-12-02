from flwr.server.strategy.fedadagrad import FedAdagrad

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedAdagradStrategy(FlowerStrategy):
    """
    Wrapper class for the FedAdagrad strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedAdagrad()

