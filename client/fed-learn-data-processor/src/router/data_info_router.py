import logging as logger
from typing import List
from fastapi.responses import JSONResponse
from fastapi import APIRouter, HTTPException

from src.model.process_request_model import DataLoadProcess, WorkflowTraceIdRequest, DataInitRequest, DataSeedMetaData, \
    DataRecordRequest
from src.repository.process.process_repository import get_data_process, get_table_data_count
from src.repository.process.seeds_repository import get_data_seed_metadata

data_info_router = APIRouter(prefix="/data-load/api")


@data_info_router.post("/local-data-process")
async def get_local_process(request: WorkflowTraceIdRequest) -> List[DataLoadProcess]:
    data_load_processes = []
    try:
        for workflow_trace_id in request.workflowTraceIds:
            process_data = get_data_process(workflow_trace_id)
            if process_data:
                data_load_processes.append(DataLoadProcess(workflowTraceId=process_data[0], status=process_data[1]))
    except Exception as e:
        logger.error(e)
    return data_load_processes


@data_info_router.post("/initial-data-seeds")
async def load_initial_data_seeds(data_init_req: DataInitRequest):
    try:
        domain_type = data_init_req.domain_type
        logger.info("Received data load request - domain type: {0} ".format(domain_type))
        data_seeds = get_data_seed_metadata(domain_type)
        return [DataSeedMetaData(id=d[0], fileName=d[1], label=d[2], domainType=domain_type) for d in data_seeds]
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@data_info_router.post("/check_data_records")
async def check_data_records(request: DataRecordRequest):
    try:
        req_workflow_trace_id = request.workflowTraceId
        status, workflow_trace_id, table, record_count = get_table_data_count(req_workflow_trace_id)
        response = {
            "status": status,
            "workflow_trace_id": workflow_trace_id,
            "table": table,
            "record_count": record_count
        }
        return JSONResponse(content=response)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
