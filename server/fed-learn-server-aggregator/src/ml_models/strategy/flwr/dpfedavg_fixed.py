from flwr.server.strategy.dpfedavg_fixed import DPFedAvgFixed

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class DPFedAvgFixedStrategy(FlowerStrategy):
    """
    Wrapper class for the DPFedAvgFixed strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = DPFedAvgFixed()

