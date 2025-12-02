from typing import Union, Dict, List, Any,Optional
from src.common.type import Parameters
from src.entities.simulation_request import SimulationTrainingRequest
from src.common.parameter import deserialize_parameters


class Metrics:
    """Model for metrics data used in federated learning."""

    def __init__(self, loss: float, accuracy: float):
        self.loss = loss
        self.accuracy = accuracy


class ClientData:
    """Data model for each client's input in federated learning."""

    def __init__(self, parameters: Union[str, Parameters], metrics: Metrics, num_examples: int):
        self.parameters = parameters
        self.metrics = metrics
        self.num_examples = num_examples

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

    def __str__(self):
        return f"ClientData(metrics=loss: {self.metrics.loss}, accuracy: {self.metrics.accuracy}, num_examples={self.num_examples})"


class Client:
    """Model for a client participating in federated learning."""

    def __init__(self, clientId: int, data: ClientData):
        self.clientId = clientId
        self.data = data

    def __str__(self):
        return f"Client(clientId={self.clientId}, data={self.data})"


class FedLearnServerRequest:
    """Common interface for federated learning server requests."""

    def __init__(self, domainType: str, num_rounds: int, strategy: str, vendor: str, clients: List[Client]):
        self.domainType = domainType
        self.num_rounds = num_rounds
        self.strategy = strategy
        self.vendor = vendor
        self.clients = clients


# Method to create ClientData for FlwrTrainingRequest
def create_flwr_client_data(parameters: Parameters, loss: Optional[float], accuracy: Optional[float], num_examples: int) -> ClientData:
    # Check if loss and accuracy are None, and create an empty metrics map if so
    if loss is None:
        metrics = {}  # Create empty map for metrics
    else:
        metrics = Metrics(loss=loss if loss is not None else 0.0, accuracy=accuracy if accuracy is not None else 0.0)

    client_data = ClientData(parameters=parameters, metrics=metrics, num_examples=num_examples)
    return client_data

def convert_to_fed_learn_server_request(request: SimulationTrainingRequest) -> FedLearnServerRequest:
    # Convert each client in the request
    strategy = request.strategy[0]
    clients = [
        Client(
            clientId=client.clientId,
            data=ClientData(
                parameters=deserialize_parameters(client.data.parameters),
                metrics=Metrics(
                    loss=client.data.metrics.loss,
                    accuracy=client.data.metrics.accuracy,
                ),
                num_examples=client.data.num_examples,
            )
        )
        for client in request.clients
    ]

    # Create and return a FedLearnServerRequest instance
    return FedLearnServerRequest(
        domainType=request.domainType,
        num_rounds=request.num_rounds,
        strategy=strategy,
        vendor=request.vendor,
        clients=clients
    )
