from typing import Dict, Any, List

import numpy as np

from src.common.parameter import ndarrays_to_parameters, parameters_to_ndarrays
from src.common.type import NDArrays
from src.entities.fl_agg_request import AggregateRequestForm
from src.entities.strategy_run_request import create_flwr_client_data, FedLearnServerRequest, Client
from src.ml_models.model_builder import decompress_weights, compress_weights, format_metrics
from src.ml_models.strategy.handler.strategy_handler import StrategyHandler
from src.repository.model.model_data_repositoty import save_model_aggregate_result
from src.util import log
from src.util.constants import FLWR_FED_AVG

logger = log.init_logger()


# pylint: disable=line-too-long

class AggregatorRunner:
    """ Class for machine learning service """

    def __init__(self):
        pass

    def aggregate(self, request: AggregateRequestForm):
        """
        Process client updates, perform federated aggregation, and persist the results.
        This method ensures that the aggregated model parameters and metrics are saved to the database.

        Args:
            request (AggregateRequestForm): The request containing client data and aggregation settings.

        Returns:
            Tuple[Optional[Parameters], Dict[str, Any]]:
                - parameters_aggregated: The aggregated model parameters, or None if aggregation failed.
                - metrics_aggregated: The aggregated metrics, or an empty dictionary if aggregation failed.
        """
        return self.run_aggregate(request, persist=True)

    def inspect_aggregated_parameters(self, request: AggregateRequestForm):
        """
        Process client updates and perform federated aggregation without persisting the results.
        This method allows for inspection of the aggregated model parameters and metrics without saving them.

        Args:
            request (AggregateRequestForm): The request containing client data and aggregation settings.

        Returns:
            Tuple[Optional[Parameters], Dict[str, Any]]:
                - parameters_aggregated: The aggregated model parameters, or None if aggregation failed.
                - metrics_aggregated: The aggregated metrics, or an empty dictionary if aggregation failed.
        """
        return self.run_aggregate(request, persist=False)

    def run_aggregate(self, request: AggregateRequestForm, persist: bool = True):
        """
          Process client updates and perform federated aggregation using the specified strategy.
          Aggregate model weights from multiple clients based on the given federated learning strategy.
          This function processes client data provided in the `AggregateRequestForm`, decodes
          and decompresses the client model parameters, and then performs aggregation using
          the specified federated learning strategy (e.g., FedAvg). The aggregated parameters
          and metrics are returned upon successful aggregation.

          Args:
              request (AggregateRequestForm): The request containing client data and aggregation settings.
              persist  provide view only option if you need inspect
          Returns:
              Tuple[Optional[Parameters], Dict[str, Any]]:
                  - parameters_aggregated: The aggregated model parameters, or None if aggregation failed.
                  - metrics_aggregated: The aggregated metrics, or an empty dictionary if aggregation failed.
          """
        strategy_list: List[str] = request.strategy
        strategy = strategy_list[0] if strategy_list else FLWR_FED_AVG
        vendor = request.vendor  # Use the vendor provided in the request
        domain = request.domain_type
        model_id = request.modelId
        current_round = request.num_rounds
        group_hash = request.groupHash
        workflow_trace_id = request.workflow_trace_id
        logger.info(
            "-------------------------------------- STARTING - AGGREGATION --------------------------------------"
        )
        logger.info(
            f"Start aggregate: Domain: {domain}, workflowTraceId: {workflow_trace_id} "
            f"strategy: {strategy}, vendor: {vendor}"
        )
        try:
            clients = []
            client_ids = []
            # Process each client in the request
            logger.info(f"w client data for aggregate_fit")
            for client_request in request.clients:
                client_id = client_request.client_id
                client_data_request = client_request.data
                parameters_encoded = client_data_request.parameters
                num_examples = client_data_request.num_examples
                logger.info("building client data - clientId: {}, num_examples: {} for aggregate_fit"
                            .format(client_id, num_examples))
                # Extract metrics if available
                metrics_model = client_data_request.metrics
                if metrics_model:
                    metrics_dict = metrics_model.dict()
                    loss = metrics_dict.get('loss')
                    accuracy = metrics_dict.get('accuracy')
                else:
                    metrics_dict = {}
                    loss = None
                    accuracy = None

                # Decode and process parameters
                try:
                    if not all([client_id, parameters_encoded, num_examples is not None]):
                        logger.error("Missing required keys in client data")
                        raise KeyError("Missing required keys in client data")

                    # Decompress and decode parameters
                    decompressed_weights = decompress_weights(parameters_encoded)
                    logger.info(f"Deserialized weights tensor: {decompressed_weights}")
                    agg_parameters = ndarrays_to_parameters(decompressed_weights)

                    # Create ClientData with decoded parameters
                    client_data = create_flwr_client_data(
                        parameters=agg_parameters, loss=loss, accuracy=accuracy, num_examples=num_examples
                    )

                    # Create Client object and add to the list
                    client = Client(clientId=client_id, data=client_data)
                    clients.append(client)
                    client_ids.append(client_id)

                except Exception as e:
                    logger.error(f"Error processing client {client_id}: {e}")
                    continue  # Skip this client and continue with others

                # Check if we have clients to aggregate
            if not clients:
                logger.error("No valid clients to aggregate")
                return None, {}

            fedlearn_request = FedLearnServerRequest(
                domainType=domain, num_rounds=current_round, strategy=strategy, vendor=vendor, clients=clients
            )
            strategy_handler = StrategyHandler(fedlearn_request)
            logger.info(f"call aggregate_fit: {domain}, round: {current_round}")
            parameters_aggregated, metrics_aggregated = strategy_handler.aggregate_fit()
            if parameters_aggregated is not None:
                logger.info(f"Saving aggregated parameters for domain: {domain}, round: {current_round}")
                client_ids_str = ','.join(str(cid) for cid in client_ids)
                if persist:
                    logger.info(f"Saving aggregated parameters to DB for domain: {domain}, round: {current_round}")
                    client_ids_str = ','.join(str(cid) for cid in client_ids)
                    save_parameters_aggregated_to_db(workflow_trace_id, client_ids_str, model_id, group_hash,
                                                     parameters_aggregated, metrics_aggregated, 0)
                else:
                    logger.info(f"Skipping saving aggregated parameters to DB as persist flag is set to False")
                readable_metrics = format_metrics(metrics_aggregated)
                logger.info(f"Aggregated Metrics: {readable_metrics}")
                return parameters_aggregated, metrics_aggregated

            else:
                logger.error(f"No fed strategy parameters from fedavg for workflow_trace_id {workflow_trace_id}")
        except Exception as e:
            logger.error(f"Error run aggregate error: {e}")
            return None, {}
        return None, {}


def save_parameters_aggregated_to_db(workflow_trace_id, client_id, model_id, group_hash, parameters_aggregated,
                                     metrics_aggregated, num_examples):
    """Save the aggregated parameters to the database."""
    try:
        parameters_compressed = compress_weights(parameters_aggregated)
        logger.info(f"{workflow_trace_id} with clients: {client_id}  save parameters weights to db DB Model weights")
        save_model_aggregate_result(workflow_trace_id, client_id, model_id, group_hash, num_examples,
                                    metrics_aggregated,
                                    parameters_compressed)
        logger.info("After calling save_model_aggregate_result")
    except Exception as e:
        logger.error(f"Error saving parameters: {e}")
        import traceback
        logger.error(traceback.format_exc())
        raise
