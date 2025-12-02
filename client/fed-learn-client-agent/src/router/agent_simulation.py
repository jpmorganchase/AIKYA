from http.client import HTTPException

from fastapi import APIRouter

from src.entities.model_simulation_request import SimulationRequest, SimulationClientRequest
from src.service.simulation.moder_simulation_runner_service import run_client_simulation, run_end_to_end_simulation
from src.util import log

logger = log.init_logger()
agent_simulation_router = APIRouter(prefix="/simulation")


@agent_simulation_router.post("/client")
async def run_client(request: SimulationRequest):
    try:
        logger.info(f"Domain Type: {request.domain_type}, File path: {request.file_path}")
        client_data = run_client_simulation(request.domain_type, request.file_path)
        if client_data is None:
            raise HTTPException()
        return {"data": client_data.dict()}
    except Exception as e:
        logger.error(f"Error run client simulation: {e}")
        raise HTTPException()


@agent_simulation_router.post("/e2e")
async def run_end_to_end(request: SimulationClientRequest):
    try:
        logger.info(f"Domain Type: {request.domain_type}, File path: {request.file_path}")
        data_req = run_end_to_end_simulation(request.domain_type, request.file_path)
        return {"predict": data_req}
    except Exception as e:
        logger.error(f"Error predict data: {e}")
        raise HTTPException()

