from http.client import HTTPException

from fastapi import APIRouter

from src.common.parameter import serialize_parameters
from src.entities.fl_agg_request import AggregateRequestForm
from src.ml_models.model_builder import format_metrics
from src.service.aggregator_runner_service import AggregatorRunner
from src.util import log
from src.util.constants import FLWR_STRATEGY_TYPES, FLWR_STRATEGY_TYPES_SUPPORT

logger = log.init_logger()
agg_router = APIRouter()


@agg_router.get("/health")
async def check_health():
    try:
        # You can add any specific health check logic here if needed
        return {"message": "Service is healthy", "success": True}
    except Exception as e:
        logger.error("Health check failed: {0}".format(str(e)))
        return {"message": "Service is not healthy", "success": False}


@agg_router.post("/run-aggregate")
async def aggregate(request: AggregateRequestForm):
    try:
        aggregator_runner = AggregatorRunner()
        aggregator_runner.aggregate(request)
        return {
            "status": "success",
            "domain": request.domain_type,
            "workflowTraceId": request.workflow_trace_id
        }
    except Exception as e:
        logger.error(f"Error during aggregation: {e}")
        raise HTTPException(status_code=500, detail="Aggregation failed")


@agg_router.post("/inspect-aggregate")
async def inspect_aggregate(request: AggregateRequestForm):
    try:
        aggregator_runner = AggregatorRunner()
        # Call the inspect_aggregated_parameters method to get the aggregated data
        parameters_aggregated, metrics_aggregated = aggregator_runner.inspect_aggregated_parameters(request)
        parameters_serializable = serialize_parameters(parameters_aggregated)
        metrics_serializable = format_metrics(metrics_aggregated)
        # Return the response including the aggregated parameters and metrics
        return {
            "status": "success",
            "domain": request.domain_type,
            "workflowTraceId": request.workflow_trace_id,
            "parametersAggregated": parameters_serializable,
            "metricsAggregated": metrics_serializable
        }
    except Exception as e:
        logger.error(f"Error during aggregation: {e}")
        raise HTTPException(status_code=500, detail="Aggregation failed")


@agg_router.get("/flwr-strategies")
async def flwr_strategies():
    """
    Endpoint to get all FLWR strategy types.
    """
    return {"flwr_strategies": FLWR_STRATEGY_TYPES, "availableStrategies": FLWR_STRATEGY_TYPES_SUPPORT}
