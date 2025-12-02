from src.apps.provider.model_default_provider import DBModelExecutorProvider
from src.apps.provider.model_simulation_provider import SimulationModelExecutorProvider
from src.ml_models.executor.model_executor import ModelExecutor
from src.util.constants import ExecutionTypes
from src.util import log

logger = log.init_logger()


def get_executor_provider(model_context):
    domain = model_context.domain_type
    try:
        if model_context.execution_type == ExecutionTypes.DB:
            return DBModelExecutorProvider()
        elif model_context.execution_type == ExecutionTypes.SIMULATION:
            return SimulationModelExecutorProvider()
        else:
            raise ValueError(f"Unknown execution type: {model_context.execution_type}")
    except Exception as e:
        logger.error(f"Error Get {domain} Model Provider: {e}")
        raise ValueError(f"Error Get Model Provider - {domain}")


def get_executor(model_context) -> ModelExecutor:
    provider = get_executor_provider(model_context)
    logger.info(" get {} provider".format(provider.name()))
    return provider.create_executor(model_context)
