from src.entities.model_request import ModelActionRequest
from src.ml_models.provider.model_context import ModelContext
from src.ml_models.provider.provider_loader import get_executor
from src.ml_models.tool.model_builder_tool import compress_weights
from src.repository.model.model_track_repository import (
    get_model_track_record,
    create_model_track_records,
    create_local_model_historical_records,
    update_local_weight_definition,
    upsert_model_process,
)
from src.service.model_context_service import get_model_weights, generate_model_json
from src.util import log
from src.util.constants import (
    ExecutionTypes,
    DEFAULT_WORKFLOW_TRACE_ID,
    ModelClassLoaderTypes,
)

logger = log.init_logger()


class ModelRunner:
    """Class for machine learning service"""

    def __init__(self):
        """
        Initialize the ModelRunner.
        """
        pass

    def initial_weights(self, name, domain, model_version):
        """
        Initialize weights for the given domain. If no global model track exists for the domain,
        it creates an entry with the same model and weight.

        Parameters:
            name (str): The name of model.
            domain (str): The domain for which to initialize weights.
            model_version (str): The version of the model.
        Returns:
            str: Compressed and encoded model weights.

        Raises:
            Exception: If there is an error in getting or processing model weights.
        """
        try:
            model_track_record = get_model_track_record(domain)
            if not model_track_record:
                logger.info(
                    "No global model track found for domain: {0}. Creating a new entry.".format(
                        domain
                    )
                )
                local_weights_version = 1
                model_json = generate_model_json(domain)
                model_weights = get_model_weights(model_json)
                # Compress and encode weights
                logger.info("Compress and encode weights '{0}'.".format(domain))
                weights_compressed = compress_weights(model_weights)
                logger.info(
                    "saving model track records for domain '{0}'.".format(domain)
                )
                create_model_track_records(
                    name,
                    model_json,
                    model_version,
                    domain,
                    weights_compressed,
                    local_weights_version,
                )
                logger.info(
                    "model track records for domain '{0}' saved.".format(domain)
                )
                create_local_model_historical_records(
                    DEFAULT_WORKFLOW_TRACE_ID, name, weights_compressed
                )
                logger.info(
                    "local model historical records for domain '{0}' saved.".format(
                        domain
                    )
                )
                return weights_compressed
            else:
                # TODO : check weights and model definition. if absent, update.

                model_json: str = (
                    model_track_record[0] if len(model_track_record) >= 1 else ""
                )
                local_model_weights = (
                    model_track_record[2] if len(model_track_record) > 1 else b""
                )

                if not model_json:
                    model_json = generate_model_json(domain)
                # check if model definition is present
                if not local_model_weights:
                    local_model_weights = compress_weights(
                        get_model_weights(model_json)
                    )

                update_local_weight_definition(
                    name,
                    model_json,
                    local_model_weights if not model_track_record[2] else None,
                )
                return local_model_weights
        except Exception as e:
            logger.error(f"Error getting model weights with compression: {e}")
            raise

    def _run_model_action(
        self,
        request: ModelActionRequest,
        action: str,
        success_status: str,
        fail_status: str,
    ):
        """
        Executes a model action (training or prediction) based on the provided request and action type.

        Parameters:
            request (ModelActionRequest): An object containing all necessary parameters for the model action.
            action (str): The type of action to perform. Valid values are 'training' and 'predict'.
            success_status (str): The status string to record upon successful completion of the action (e.g., 'Complete').
            fail_status (str): The status string to record if the action fails (e.g., 'Fail').
        Notes:
          - This is a helper method intended for internal use within the ModelRunner class.
          - It centralizes the shared logic between training and prediction actions to reduce code duplication.
          - The method handles both success and failure scenarios, updating the workflow process accordingly.
        """
        workflow_trace_id = request.workflow_trace_id
        domain_type = request.domain_type
        batch_id = request.batch_id

        logger.info(
            f"Model {action} - running model {action} for domain {domain_type}, workflow_trace_id: {workflow_trace_id}, batchId: {batch_id}"
        )

        try:
            executor = get_db_executor(
                domain_type, ModelClassLoaderTypes.DB, "", workflow_trace_id, batch_id
            )

            # Call the appropriate executor method based on action
            if action == "training":
                status, trace_id, loss, num_examples, metrics = executor.run_training()
                if status == "success":
                    logger.info(
                        f"Training successful. Trace ID: {trace_id}, Loss: {loss}, Number of Examples: {num_examples}, Metrics: {metrics}"
                    )
                else:
                    logger.error(f"Training failed. Trace ID: {trace_id}")
                    # Handle failure if needed
            elif action == "predict":
                result = executor.run_predict()
                num_predictions = len(result)
                logger.info(
                    f"Prediction successful. Number of predictions: {num_predictions}"
                )
            else:
                logger.error(f"Unknown action: {action}")
                return "fail", workflow_trace_id, None

            # Return results based on action
            if action == "training":
                upsert_model_process(workflow_trace_id, action, success_status)
                return status, workflow_trace_id, loss, num_examples, metrics
            elif action == "predict":
                upsert_model_process(workflow_trace_id, action, success_status)
                return "success", workflow_trace_id, num_predictions

        except Exception as e:
            logger.error(
                f"Error running model {action} for workflow_trace_id {workflow_trace_id}: {e}"
            )
            # Return failure results based on action
            if action == "training":
                upsert_model_process(workflow_trace_id, action, fail_status)
                return "fail", workflow_trace_id, None, 0, None
            elif action == "predict":
                upsert_model_process(workflow_trace_id, action, fail_status)
                return "fail", workflow_trace_id, 0

    def run_model_training(self, request: ModelActionRequest):
        """
        Run the model training action.

        Args:
            request (ModelActionRequest): The request object containing training parameters.

        Returns:
            Tuple: A tuple containing status, workflow_trace_id, loss, num_examples, and metrics.
        """
        return self._run_model_action(
            request, action="training", success_status="Complete", fail_status="Fail"
        )

    def run_model_predict(self, request: ModelActionRequest):
        """
        Run the model prediction action.

        Args:
            request (ModelActionRequest): The request object containing prediction parameters.

        Returns:
            Tuple: A tuple containing status, workflow_trace_id, and number of predictions.
        """
        return self._run_model_action(
            request, action="predict", success_status="Complete", fail_status="Fail"
        )


def get_db_executor(
    domain_type, model_class_type, file_path, workflow_trace_id, batch_id
):
    model_context = ModelContext(
        domain_type=domain_type,
        execution_type=ExecutionTypes.DB,
        model_class_type=model_class_type,
        file_path=file_path,
        workflow_trace_id=workflow_trace_id,
        batch_id=batch_id,
    )
    return get_executor(model_context)
