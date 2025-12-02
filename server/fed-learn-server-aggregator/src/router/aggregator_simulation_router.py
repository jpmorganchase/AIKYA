from http.client import HTTPException

from fastapi import APIRouter

from src.common.parameter import serialize_parameters
from src.entities.simulation_request import SimulationTrainingRequest
from src.service.aggregator_runner_service import format_metrics
from src.service.aggregator_simulation_runner import AggregatorSimulationRunner
from src.util import log

logger = log.init_logger()
agg_simulation_router = APIRouter(prefix="/simulation")


@agg_simulation_router.post("/run-aggregate")
async def aggregate(request: SimulationTrainingRequest):
    """
    Simulate the training process in a federated learning environment.

    Args:
        request (TrainingRequest): The training request containing domain type, number of rounds, strategy, and clients.

    Returns:
        dict: A dictionary containing the status of the training initiation.
    """
    try:
        aggregator_runner = AggregatorSimulationRunner()
        parameters_aggregated, metrics_aggregated = aggregator_runner.aggregate(request)
        parameters_serializable = serialize_parameters(parameters_aggregated)
        metrics_serializable = format_metrics(metrics_aggregated)
        return {
            "status": "training completed",
            "parameters_aggregated": parameters_serializable,
            "metrics_aggregated": metrics_serializable
        }
    except Exception as e:
        logger.error(f"Error aggregate fit: {e}")
        raise HTTPException()


