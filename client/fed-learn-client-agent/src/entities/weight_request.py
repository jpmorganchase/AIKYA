from pydantic import BaseModel, Field


class WeightRequest(BaseModel):
    name: str = Field(..., alias="name", description="model name")
    domain: str = Field(..., alias="domain", description="model domain name")
    version: str = Field(..., alias="version", description="model version")


class WeightModelRequest(BaseModel):
    name: str = Field(..., alias="name", description="model name")
    model: str = Field(..., alias="model", description="model json format")
    domain: str = Field(..., alias="domain", description="model domain name")
    version: str = Field(..., alias="version", description="model version")
