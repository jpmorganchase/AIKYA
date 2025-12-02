from src.ml_models.executor.model_executor import ModelExecutor
from src.ml_models.provider.model_context import ModelContext
from src.ml_models.provider.provider_loader import get_executor
from src.util import log
from src.util.constants import ExecutionTypes, ModelClassLoaderTypes

logger = log.init_logger()


class ModelSimulationRunner:
    """Class for machine learning service"""

    def __init__(self):
        """Initialize the ModelSimulationRunner."""
        pass


def run_client_simulation(domain_type, file_path):
    try:
        logger.info(
            "run client simulation {0}, file_path: {1}".format(domain_type, file_path)
        )
        executor = get_simulation_executor(domain_type, file_path)
        result = executor.run()
        return result
    except Exception as e:
        logger.error(f"Error running model client simulation: {e}")
        return None


def run_end_to_end_simulation(domain_type, file_path):
    try:
        executor = get_simulation_executor(domain_type, file_path)
        loss, accuracy, metrics, num_examples = executor.run()
        return loss, accuracy, num_examples, metrics
    except Exception as e:
        logger.error(f"Error run model predict: {e}")
        return 0, 0, 0, None


def get_simulation_executor(domain_type, file_path) -> ModelExecutor:
    model_context = ModelContext(
        domain_type, ExecutionTypes.SIMULATION, ModelClassLoaderTypes.CLASS, file_path
    )
    return get_executor(model_context)
