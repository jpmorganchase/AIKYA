from flwr.server.strategy.bulyan import Bulyan
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy


class BulyanStrategy(FlowerStrategy):
    """
    Wrapper class for the Bulyan strategy in the Flower framework.
    """

    def __init__(self):
        super().__init__()
        self.strategy = Bulyan()

