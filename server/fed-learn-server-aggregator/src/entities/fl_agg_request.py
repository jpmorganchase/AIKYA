from typing import List, Optional, Dict
from pydantic import BaseModel, Field


class ClientDataModel(BaseModel):
    """Data model for each client's input in federated learning."""
    parameters: str = Field(..., description="Model parameters")
    metrics: Optional[Dict] = Field(default_factory=dict, description="Metrics data")
    num_examples: int = Field(..., alias="num_examples", description="Number of examples")


class ClientModel(BaseModel):
    """Model for a client participating in federated learning."""
    client_id: int = Field(..., alias="clientId", description="Client ID")
    data: ClientDataModel = Field(..., description="Client data")


class AggregateRequestForm(BaseModel):
    """Request model for federated learning training."""
    domain_type: str = Field(..., alias="domainType", description="Model domain type")
    num_rounds: int = Field(..., alias="num_rounds", description="Number of rounds")
    strategy: List[str] = Field(..., description="Federated learning strategies")
    vendor: str = Field(..., description="FL Vendor")
    clients: List[ClientModel] = Field(..., description="List of clients")
    workflow_trace_id: str = Field(..., alias="workflowTraceId", description="Workflow trace ID")
    modelId: int = Field(..., alias="modelId", description="model id")
    groupHash: str = Field(..., description="FL Run Group Hash")

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "domainType": "image_classification",
                "num_rounds": 10,
                "strategy": ["strategy1", "strategy2"],
                "vendor": "FL Vendor X",
                "clients": [
                    {
                        "clientId": 1,
                        "data": {
                            "parameters": "params1",
                            "metrics": {
                                "loss": 0.5,
                                "accuracy": 0.8
                            },
                            "num_examples": 100
                        }
                    }
                ],
                "modelId": 2,
                "workflowTraceId": "trace123"
            }
        }
