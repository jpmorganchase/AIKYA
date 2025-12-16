from src.entities.simulation_request import SimulationTrainingRequest
from src.entities.strategy_run_request import convert_to_fed_learn_server_request
from src.ml_models.strategy.handler.strategy_handler import StrategyHandler
from src.util import log

logger = log.init_logger()


# pylint: disable=line-too-long
class AggregatorSimulationRunner:
    """ Class for machine learning service """

    def __init__(self):
        pass

    def aggregate(self, request: SimulationTrainingRequest):
        """
        Simulate the training process in a federated learning environment.

        Args:
            request (TrainingRequest): The training request containing domain type, number of rounds, strategy, and clients.

        Returns:
            dict: A dictionary containing the status of the training initiation.
        """
        try:
            # Convert SimulationTrainingRequest to FedLearnServerRequest
            fed_learn_request = convert_to_fed_learn_server_request(request)
            logger.info("Starting fed learning under Simulation mode")
            strategy_handler = StrategyHandler(fed_learn_request)
            parameters_aggregated, metrics_aggregated = strategy_handler.aggregate_fit()
            return parameters_aggregated, metrics_aggregated
        except Exception as e:
            logger.error(f"Error during aggregate_fit: {str(e)}")
            # Provide default empty return values
            return None, {}
