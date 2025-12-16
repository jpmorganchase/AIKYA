from typing import Dict, Any

from flwr.common import Code, FitRes, Status
from flwr.common.typing import Union, Metrics

from src.common.aikya_custom_client_proxy import CustomClientProxy
from src.entities.strategy_run_request import FedLearnServerRequest
from src.ml_models.strategy.provider.strategy_provider import StrategyProvider
from src.util import log

logger = log.init_logger()


class FlwrStrategyHandler:
    """
    Handler for Flower strategy.
    """

    def __init__(self, request: FedLearnServerRequest):
        self.request = request

    def aggregate_fit(self) -> tuple[Any, Dict[str, Any]]:
        vendor = self.request.vendor
        strategy_name = self.request.strategy  # Use the first strategy for now
        clients = self.request.clients
        results: list[tuple[CustomClientProxy, FitRes]] = []
        failures: list[Union[tuple[CustomClientProxy, FitRes], BaseException]] = []
        logger.info(
            f"Starting aggregation for vendor: {vendor}, strategy: {strategy_name}, clients: {clients}"
        )

        for client in clients:
            client_id = client.clientId
            client_data = client.data
            parameters = client_data.parameters

            # Use empty metrics dictionary if client_data.metrics is None or empty
            metrics = (
                {
                    "loss": client_data.metrics.loss if client_data.metrics else None,
                    "num_examples": client_data.num_examples,
                    "accuracy": client_data.metrics.accuracy
                    if client_data.metrics
                    else None,
                }
                if client_data.metrics
                else {"num_examples": client_data.num_examples}
            )

            num_examples = client_data.num_examples
            client_proxy = CustomClientProxy(cid=str(client_id))
            fit_res = FitRes(
                status=Status(code=Code.OK, message="Success"),
                parameters=parameters,
                num_examples=num_examples,
                metrics={},
            )
            results.append((client_proxy, fit_res))

        logger.info(
            f"****** Staring {vendor} -> strategy {strategy_name} aggregation, clients: {len(results)} ******"
        )
        provider = StrategyProvider.get_strategy_vendor(vendor)
        strategy = provider.create_strategy(strategy_name)
        try:
            parameters_aggregated, metrics_aggregated = strategy.aggregate_fit(
                self.request.num_rounds, results, failures
            )
            return parameters_aggregated, metrics_aggregated
        except Exception as e:
            logger.error(
                f"Error {vendor}: strategy {strategy_name}  aggregate_fit: {e}"
            )
            raise
        logger.info(
            f"****** Complete {vendor} -> strategy {strategy_name} aggregation, clients: {len(results)} ******"
        )
        # logger.info(f"Aggregated metrics: {metrics_aggregated}")

    def aggregate_evaluate(self) -> tuple[Any, Dict[str, Any]]:
        # Implement the evaluation aggregation logic for Flower strategy
        pass


def weighted_metrics_average(metrics):
    logger.info(f"weighted_metrics_average: {metrics}")
    # Check if there are any valid metrics, return None if no metrics are present
    if not metrics or len(metrics) == 0:
        return None

    # Safely calculate accuracy, using 0 as the default if "accuracy" is missing
    accuracies = [num_examples * m.get("accuracy", 0) for num_examples, m in metrics]

    # Safely calculate loss, using 0 as the default if "loss" is missing
    losses = [num_examples * m.get("loss", 0) for num_examples, m in metrics]

    # Get the number of examples
    examples = [num_examples for num_examples, _ in metrics]

    # Calculate total number of examples
    total_examples = sum(examples)

    # Avoid division by zero by checking total_examples
    if total_examples == 0:
        return {"accuracy": None, "loss": None}

    # Calculate weighted average for accuracy and loss
    avg_accuracy = sum(accuracies) / total_examples
    avg_loss = sum(losses) / total_examples if sum(losses) > 0 else None

    # Construct the result dictionary
    result = {"accuracy": avg_accuracy}
    if avg_loss is not None:
        result["loss"] = avg_loss

    return result


def weighted_average(metrics: list[tuple[int, Metrics]]) -> Metrics:
    logger.info(" set up weighted_average")
    # Calculate weighted accuracies
    accuracies = [num_examples * m["accuracy"] for num_examples, m in metrics]
    examples = [num_examples for num_examples, _ in metrics]

    # Log each client's accuracy and the number of examples used
    for i, (acc, ex) in enumerate(zip(accuracies, examples)):
        logger.info(f"Client {i}: Accuracy={acc / ex}, Examples={ex}")

    total_examples = sum(examples)
    total_weighted_accuracy = sum(accuracies)
    weighted_avg_accuracy = total_weighted_accuracy / total_examples
    # Log aggregate information
    logger.info(f"Total Examples: {total_examples}")
    logger.info(f"Total Weighted Accuracy: {total_weighted_accuracy}")
    logger.info(f"Weighted Average Accuracy: {weighted_avg_accuracy}")

    # Aggregate and return custom metric (weighted average)
    return {"accuracy": weighted_avg_accuracy}
