from typing import List

from pydantic import BaseModel, Field


class SimulationRequest(BaseModel):
    domain_type: str = Field(..., alias="domainType", description="model domain type")
    file_path: str = Field(..., alias="filePath", description="file path (csv)")


class Metrics(BaseModel):
    loss: float = Field(..., description="Loss metric")
    accuracy: float = Field(..., description="Accuracy metric")


class ClientData(BaseModel):
    parameters: str = Field(..., description="Model parameters")
    metrics: Metrics = Field(..., description="Metrics data")
    num_examples: int = Field(..., alias="num_examples", description="Number of examples")


class Client(BaseModel):
    client_id: int = Field(..., alias="clientId", description="Client ID")
    data: ClientData = Field(..., description="Client data")


class SimulationClientRequest(BaseModel):
    domain_type: str = Field(..., alias="domainType", description="Model domain type")
    num_rounds: int = Field(..., alias="numRounds", description="Number of rounds")
    strategy: List[str] = Field(..., description="Strategy list")
    clients: List[Client] = Field(..., description="List of clients")
