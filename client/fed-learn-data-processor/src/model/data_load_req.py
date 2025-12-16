from pydantic import BaseModel, Field


class DataLoadRequest(BaseModel):
    file_name: str = Field(..., alias="fileName", description="data seed file name")
    domain_type: str = Field(..., alias="domainType", description="data seed domain type")
    mock_enabled: str = Field(..., alias="mockerEnabled", description="mock data enable")
    workflow_trace_id: str = Field(..., alias="workflowTraceId", description="workflow trace id")
    batch_id: str = Field(..., alias="batchId", description="batch id")
    model: str = Field(..., alias="model", description="model name")
