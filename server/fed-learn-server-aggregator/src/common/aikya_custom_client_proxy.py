from flwr.server.client_proxy import ClientProxy
from typing import Optional

from flwr.common import (
    Code,
    DisconnectRes,
    EvaluateIns,
    EvaluateRes,
    FitIns,
    FitRes,
    GetParametersIns,
    GetParametersRes,
    GetPropertiesIns,
    GetPropertiesRes,
    Parameters,
    ReconnectIns,
    Status,
)

class CustomClientProxy(ClientProxy):
    """Subclass of ClientProxy."""

    def get_properties(
        self,
        ins: GetPropertiesIns,
        timeout: Optional[float],
        group_id: Optional[int],
    ) -> GetPropertiesRes:
        """Return the client's properties."""
        return GetPropertiesRes(status=Status(code=Code.OK, message=""), properties={})

    def get_parameters(
        self,
        ins: GetParametersIns,
        timeout: Optional[float],
        group_id: Optional[int],
    ) -> GetParametersRes:
        """Return the current local model parameters."""
        return GetParametersRes(
            status=Status(code=Code.OK, message=""),
            parameters=Parameters(tensors=[], tensor_type=""),
        )

    def fit(
        self,
        ins: FitIns,
        timeout: Optional[float],
        group_id: Optional[int],
    ) -> FitRes:
        """Refine the provided weights using the locally held dataset."""
        return FitRes(
            status=Status(Code.OK, message=""),
            parameters=Parameters(tensors=[], tensor_type=""),
            num_examples=0,
            metrics={},
        )

    def evaluate(
        self,
        ins: EvaluateIns,
        timeout: Optional[float],
        group_id: Optional[int],
    ) -> EvaluateRes:
        """Evaluate the provided weights using the locally held dataset."""
        return EvaluateRes(
            status=Status(Code.OK, message=""), loss=0.0, num_examples=0, metrics={}
        )

    def reconnect(
        self,
        ins: ReconnectIns,
        timeout: Optional[float],
        group_id: Optional[int],
    ) -> DisconnectRes:
        """Disconnect and (optionally) reconnect later."""
        return DisconnectRes(reason="")