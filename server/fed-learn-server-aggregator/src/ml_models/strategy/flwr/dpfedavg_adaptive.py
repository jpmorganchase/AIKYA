from flwr.server.strategy.dpfedavg_adaptive import DPFedAvgAdaptive

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class DPFedAvgAdaptiveStrategy(FlowerStrategy):
    """
    Wrapper class for the DPFedAvgAdaptive strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = DPFedAvgAdaptive()

