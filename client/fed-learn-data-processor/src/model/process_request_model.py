from typing import List

from pydantic import BaseModel, Field


class DataLoadProcess(BaseModel):
    workflowTraceId: str
    status: str


class WorkflowTraceIdRequest(BaseModel):
    workflowTraceIds: List[str]


class DataRecordRequest(BaseModel):
    workflowTraceId: str


class DataInitRequest(BaseModel):
    domain_type: str = Field(..., alias="domainType", description="data seed domain type")


class DataSeedMetaData(BaseModel):
    id: int
    fileName: str
    label: str
    domainType: str
