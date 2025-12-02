from flwr.server.strategy.qfedavg import QFedAvg

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class QFedAvgStrategy(FlowerStrategy):
    """
    Wrapper class for the QFedAvg strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = QFedAvg()

