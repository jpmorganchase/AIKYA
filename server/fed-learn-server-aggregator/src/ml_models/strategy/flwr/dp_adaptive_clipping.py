from flwr.server.strategy.dp_adaptive_clipping import DifferentialPrivacyServerSideAdaptiveClipping

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class DifferentialPrivacyServerSideAdaptiveClippingStrategy(FlowerStrategy):
    """
    Wrapper class for the DifferentialPrivacyServerSideAdaptiveClipping strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = DifferentialPrivacyServerSideAdaptiveClipping()

