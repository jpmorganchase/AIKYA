from typing import List
import logging as logger
from fastapi import APIRouter, HTTPException

from src.model.data_load_req import DataLoadRequest
from src.service.processor.data_process_executor import DataProcessExecutor

data_load_router = APIRouter(prefix="/data-load/api")


@data_load_router.get("/health")
async def check_health():
    try:
        # You can add any specific health check logic here if needed
        return {"message": "Service is healthy", "success": True}
    except Exception as e:
        logger.error("Health check failed: {0}".format(str(e)))
        return {"message": "Service is not healthy", "success": False}


@data_load_router.post("/loadLocalData")
async def load_local_data(data_load_req: DataLoadRequest):
    # Log or process the received data
    try:
        # Log or process the received data
        logger.info(
            "-------load_local_data: Received data load request {0}, domain type: {1} ------"
            .format(data_load_req.file_name, data_load_req.domain_type))
        executor = DataProcessExecutor(data_load_req)
        executor.execute("seed")
        return {"message": "Load data", "success": True}
    except Exception as e:
        logger.error("Failed to load data: {0}".format(str(e)))
        return {"message": "Failed to load data", "success": False}

