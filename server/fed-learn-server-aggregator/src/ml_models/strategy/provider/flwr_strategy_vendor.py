from src.ml_models.strategy.flwr.bulyan import BulyanStrategy
from src.ml_models.strategy.flwr.dp_adaptive_clipping import DifferentialPrivacyServerSideAdaptiveClippingStrategy
from src.ml_models.strategy.flwr.dp_fixed_clipping import DifferentialPrivacyServerSideFixedClippingStrategy
from src.ml_models.strategy.flwr.dpfedavg_adaptive import DPFedAvgAdaptiveStrategy
from src.ml_models.strategy.flwr.dpfedavg_fixed import DPFedAvgFixedStrategy
from src.ml_models.strategy.flwr.fault_tolerant_fedavg import FaultTolerantFedAvgStrategy
from src.ml_models.strategy.flwr.fedadagrad import FedAdagradStrategy
from src.ml_models.strategy.flwr.fedadam import FedAdamStrategy
from src.ml_models.strategy.flwr.fedavg import FedAvgStrategy
from src.ml_models.strategy.flwr.fedavg_android import FedAvgAndroidStrategy
from src.ml_models.strategy.flwr.fedavgm import FedAvgMStrategy
from src.ml_models.strategy.flwr.fedmedian import FedMedianStrategy
from src.ml_models.strategy.flwr.fedopt import FedOptStrategy
from src.ml_models.strategy.flwr.fedprox import FedProxStrategy
from src.ml_models.strategy.flwr.fedtrimmedavg import FedTrimmedAvgStrategy
from src.ml_models.strategy.flwr.fedxgb_bagging import FedXgbBaggingStrategy
from src.ml_models.strategy.flwr.fedxgb_cyclic import FedXgbCyclicStrategy
from src.ml_models.strategy.flwr.fedxgb_nn_avg import FedXgbNnAvgStrategy
from src.ml_models.strategy.flwr.fedyogi import FedYogiStrategy
from src.ml_models.strategy.flwr.flwr_strategy import FlowerStrategy
from src.ml_models.strategy.flwr.krum import KrumStrategy
from src.ml_models.strategy.flwr.qfedavg import QFedAvgStrategy
from src.ml_models.strategy.provider.strategy_vendor import StrategyVendor
from src.util.constants import *
from src.util import log
logger = log.init_logger()

class FlwrStrategyVendor(StrategyVendor):
    @staticmethod
    def create_strategy(strategy_type: str) -> FlowerStrategy:
        """
        Static method to create a FlowerStrategy instance based on the strategy type.

        :param strategy_type: str, the type of strategy to create such as: ('FedAvg')
        :return: FlowerStrategy, an instance of a concrete FlowerStrategy
        :raises ValueError: if the strategy type is unknown
        """
        logger.info(f"creating  flwr {strategy_type} strategy")
        if strategy_type == FLWR_BULYAN:
            return BulyanStrategy()
        elif strategy_type == FLWR_DIFFERENTIAL_PRIVACY_SERVER_SIDE_ADAPTIVE_CLIPPING:
            return DifferentialPrivacyServerSideAdaptiveClippingStrategy()
        elif strategy_type == FLWR_DIFFERENTIAL_PRIVACY_SERVER_SIDE_FIXED_CLIPPING:
            return DifferentialPrivacyServerSideFixedClippingStrategy()
        elif strategy_type == FLWR_DP_FED_AVG_ADAPTIVE:
            return DPFedAvgAdaptiveStrategy()
        elif strategy_type == FLWR_DP_FED_AVG_FIXED:
            return DPFedAvgFixedStrategy()
        elif strategy_type == FLWR_FAULT_TOLERANT_FED_AVG:
            return FaultTolerantFedAvgStrategy()
        elif strategy_type == FLWR_FED_ADAGRAD:
            return FedAdagradStrategy()
        elif strategy_type == FLWR_FED_ADAM:
            return FedAdamStrategy()
        elif strategy_type == FLWR_FED_AVG_ANDROID:
            return FedAvgAndroidStrategy()
        elif strategy_type == FLWR_FED_AVG:
            return FedAvgStrategy()
        elif strategy_type == FLWR_FED_AVG_M:
            return FedAvgMStrategy()
        elif strategy_type == FLWR_FED_MEDIAN:
            return FedMedianStrategy()
        elif strategy_type == FLWR_FED_OPT:
            return FedOptStrategy()
        elif strategy_type == FLWR_FED_PROX:
            return FedProxStrategy()
        elif strategy_type == FLWR_FED_TRIMMED_AVG:
            return FedTrimmedAvgStrategy()
        elif strategy_type == FLWR_FED_XGB_BAGGING:
            return FedXgbBaggingStrategy()
        elif strategy_type == FLWR_FED_XGB_CYCLIC:
            return FedXgbCyclicStrategy()
        elif strategy_type == FLWR_FED_XGB_NN_AVG:
            return FedXgbNnAvgStrategy()
        elif strategy_type == FLWR_FED_YOGI:
            return FedYogiStrategy()
        elif strategy_type == FLWR_KRUM:
            return KrumStrategy()
        elif strategy_type == FLWR_Q_FED_AVG:
            return QFedAvgStrategy()
        else:
            raise ValueError(f"Unknown strategy type: {strategy_type}")
