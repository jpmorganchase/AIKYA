from http.client import HTTPException

from fastapi import APIRouter

from src.entities.model_request import PredictRequest, ModelActionRequest
from src.entities.weight_request import WeightRequest
from src.service.moder_runner_service import ModelRunner
from src.util import log

logger = log.init_logger()
agent_router = APIRouter()


@agent_router.get("/health")
async def check_health():
    try:
        # You can add any specific health check logic here if needed
        return {"message": "Service is healthy", "success": True}
    except Exception as e:
        logger.error("Health check failed: {0}".format(str(e)))
        return {"message": "Service is not healthy", "success": False}


@agent_router.post("/initial-weights")
async def initial_weights(request: WeightRequest):
    try:
        model_runner = ModelRunner()
        weights = model_runner.initial_weights(request.name, request.domain, request.version)
        return {"status": "success", "domain": request.domain, "weights": weights}
    except Exception as e:
        logger.error(f"Error getting model weights: {e}")
        raise HTTPException()


@agent_router.post("/predict")
async def predict_data(request: ModelActionRequest):
    """
    Endpoint to initiate model prediction.

    Args:
        request (ModelActionRequest): The request body containing prediction parameters.

    Returns:
        dict: A dictionary containing the status and prediction results.
    """
    try:
        logger.info(f"Received prediction request: {request}")
        model_runner = ModelRunner()
        status, workflow_trace_id, num_predictions = model_runner.run_model_predict(request)
        return {
            "status": status,
            "workflow_trace_id": workflow_trace_id,
            "num_predictions": num_predictions
        }
    except Exception as e:
        logger.error(f"Error in predict data: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@agent_router.post("/training")
async def training_data(request: ModelActionRequest):
    """
    Endpoint to initiate model training.

    Args:
        request (ModelActionRequest): The request body containing training parameters.

    Returns:
        dict: A dictionary containing the status and training results.
    """
    try:
        logger.info(f"Received training request: {request}")
        model_runner = ModelRunner()
        status, workflow_trace_id, loss, num_examples, metrics = model_runner.run_model_training(request)
        return {
            "status": status,
            "workflow_trace_id": workflow_trace_id,
            "loss": loss,
            "num_examples": num_examples,
            "metrics": metrics
        }
    except Exception as e:
        logger.error(f"Error in training data: {e}")
        raise HTTPException(status_code=500, detail=str(e))
