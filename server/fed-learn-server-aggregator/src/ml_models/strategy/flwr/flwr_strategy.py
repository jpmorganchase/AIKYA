from abc import ABC
from typing import Dict, Optional

from flwr.common import (
    EvaluateIns,
    EvaluateRes,
    FitRes,
    Parameters,
    Scalar,
)
from flwr.common.typing import Union
from flwr.server.client_manager import ClientManager
from flwr.server.client_proxy import ClientProxy
from flwr.server.strategy import Strategy
from src.util import log

logger = log.init_logger()


class FlowerStrategy(ABC):
    """
    Base class for Flower strategies. This class implements default behavior
    for all methods, delegating calls to the self.strategy instance.
    """

    def __init__(self):
        self.strategy: Optional[Strategy] = None

    def _check_strategy(self):
        logger.info(f"checking  Strategy {self.strategy}")
        if self.strategy is None:
            raise ValueError(
                "Strategy not defined. Ensure 'self.strategy' is set in the subclass."
            )

    def initialize_parameters(
        self, client_manager: ClientManager
    ) -> Optional[Parameters]:
        """
        Initialize model parameters.

        :param client_manager: ClientManager, the manager for client communication
        :return: Optional[Parameters], the initial parameters for the model
        """
        self._check_strategy()
        logger.info(f"run strategy: {self.strategy} initialize_parameters")
        return self.strategy.initialize_parameters(client_manager)

    def evaluate(
        self, server_round: int, parameters: Parameters
    ) -> Optional[tuple[float, Dict[str, Scalar]]]:
        """
        Evaluate the current model parameters.

        :param server_round: int, the current round of training
        :param parameters: Parameters, the current model parameters
        :return: Optional[Tuple[float, Dict[str, Scalar]]], evaluation loss and metrics
        """
        self._check_strategy()
        logger.info(f"run strategy: {self.strategy} evaluate")
        return self.strategy.evaluate(server_round, parameters)

    def configure_evaluate(
        self, server_round: int, parameters: Parameters, client_manager: ClientManager
    ) -> list[tuple[ClientProxy, EvaluateIns]]:
        """
        Configure the evaluation task for clients.

        :param server_round: int, the current round of training
        :param parameters: Parameters, the current model parameters
        :param client_manager: ClientManager, the manager for client communication
        :return: List[Tuple[ClientProxy, EvaluateIns]], list of evaluation instructions for clients
        """
        self._check_strategy()
        logger.info(f"run strategy: {self.strategy} configure_evaluate")
        return self.strategy.configure_evaluate(
            server_round, parameters, client_manager
        )

    def aggregate_fit(
        self,
        server_round: int,
        results: list[tuple[ClientProxy, FitRes]],
        failures: list[Union[tuple[ClientProxy, FitRes], BaseException]],
    ) -> tuple[Optional[Parameters], Dict[str, Scalar]]:
        """
        Aggregate fit results from clients.

        :param server_round: int, the current round of training
        :param results: List[Tuple[ClientProxy, FitRes]], list of fit results from clients
        :param failures: List[Union[Tuple[ClientProxy, FitRes], BaseException]], list of failures
        :return: Tuple[Optional[Parameters], Dict[str, Scalar]], aggregated parameters and metrics
        """
        self._check_strategy()
        logger.info(f"run strategy: {self.strategy} aggregate_fit")
        return self.strategy.aggregate_fit(server_round, results, failures)

    def aggregate_evaluate(
        self,
        server_round: int,
        results: list[tuple[ClientProxy, EvaluateRes]],
        failures: list[Union[tuple[ClientProxy, EvaluateRes], BaseException]],
    ) -> tuple[Optional[float], Dict[str, Scalar]]:
        """
        Aggregate evaluation results from clients.

        :param server_round: int, the current round of training
        :param results: List[Tuple[ClientProxy, EvaluateRes]], list of evaluation results from clients
        :param failures: List[Union[Tuple[ClientProxy, EvaluateRes], BaseException]], list of failures
        :return: Tuple[Optional[float], Dict[str, Scalar]], aggregated loss and metrics
        """
        self._check_strategy()
        logger.info(f"run strategy: {self.strategy} aggregate_evaluate")
        return self.strategy.aggregate_evaluate(server_round, results, failures)
