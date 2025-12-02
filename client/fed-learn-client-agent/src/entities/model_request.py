from typing import List

from pydantic import BaseModel, Field


class DataItem(BaseModel):
    features: List[float] = Field(..., description="Prediction data features")


class PredictRequest(BaseModel):
    domain_type: str = Field(
        ..., alias="domainType", description="data seed domain type"
    )
    workflow_trace_id: str = Field(
        ..., alias="workflowTraceId", description="workflow trace id"
    )
    batch_id: str = Field(..., alias="batchId", description="batch id")


class TrainingRequest(BaseModel):
    domain_type: str = Field(
        ..., alias="domainType", description="data seed domain type"
    )
    workflow_trace_id: str = Field(
        ..., alias="workflowTraceId", description="workflow trace id"
    )
    batch_id: str = Field(..., alias="batchId", description="batch id")
    workflow_enable: bool = Field(
        True, alias="workflowEnable", description="Enable or disable the workflow"
    )


class ModelActionRequest(BaseModel):
    domain_type: str = Field(
        ..., alias="domainType", description="Data seed domain type"
    )
    workflow_trace_id: str = Field(
        ..., alias="workflowTraceId", description="Workflow trace ID"
    )
    batch_id: str = Field(..., alias="batchId", description="Batch ID")


class ModelInitRequest(BaseModel):
    domain_type: str = Field(
        ..., alias="domainType", description="data seed domain type"
    )
