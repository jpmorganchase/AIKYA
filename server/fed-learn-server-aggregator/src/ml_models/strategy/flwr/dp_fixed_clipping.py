from flwr.server.strategy.dp_fixed_clipping import DifferentialPrivacyServerSideFixedClipping

from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class DifferentialPrivacyServerSideFixedClippingStrategy(FlowerStrategy):
    """
    Wrapper class for the DifferentialPrivacyServerSideFixedClipping strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = DifferentialPrivacyServerSideFixedClipping()

