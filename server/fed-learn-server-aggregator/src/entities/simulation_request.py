from typing import List, Dict, Any

from pydantic import BaseModel, Field


class MetricsModel(BaseModel):
    """Model for metrics data used in federated learning."""
    loss: float = Field(..., description="Loss metric")
    accuracy: float = Field(..., description="Accuracy metric")


class ClientDataModel(BaseModel):
    """Data model for each client's input in federated learning."""
    parameters: str = Field(..., description="Model parameters")
    metrics: MetricsModel = Field(..., description="Metrics data")
    num_examples: int = Field(..., description="Number of examples")

    def to_dict(self) -> Dict[str, Any]:
        """
        Convert the ClientData instance to a dictionary format suitable for federated learning.
        """
        metrics_dict = {
            "loss": self.metrics.loss,
            "num_examples": self.num_examples,
            "accuracy": self.metrics.accuracy,
        }
        return {
            "parameters": self.parameters,
            "metrics": metrics_dict,
        }


class ClientModel(BaseModel):
    """Model for a client participating in federated learning."""
    clientId: int = Field(..., alias="clientId", description="Client ID")
    data: ClientDataModel = Field(..., description="Client data")


class SimulationTrainingRequest(BaseModel):
    """Request model for federated learning training."""
    domainType: str = Field(..., alias="domainType", description="Model domain type")
    num_rounds: int = Field(..., alias="num_rounds", description="Number of rounds")
    strategy: List[str] = Field(..., description="Federated learning strategy")
    vendor: str = Field(...,  description="FL Vendor")
    clients: List[ClientModel] = Field(..., description="List of clients")

    class ConfigDict:
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
                                "accuracy": 0.8,
                            },
                            "num_examples": 100,
                        }
                    }
                ]
            }
        }
