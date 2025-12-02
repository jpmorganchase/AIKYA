from flwr.server.strategy.fedxgb_cyclic import FedXgbCyclic

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class FedXgbCyclicStrategy(FlowerStrategy):
    """
    Wrapper class for the FedXgbCyclic strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = FedXgbCyclic()

