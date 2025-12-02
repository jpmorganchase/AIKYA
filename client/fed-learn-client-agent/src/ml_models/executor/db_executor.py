import math
from abc import abstractmethod

import numpy as np
import pandas as pd

from src.ml_models.executor.model_executor import ModelExecutor
from src.ml_models.tool.model_builder_tool import compress_weights
from src.repository.db.db_connection import DBConnection
from src.repository.model.model_data_repositoty import get_model_feature_record, get_model_training_record
from src.repository.model.model_track_repository import create_workflow_model_process, \
    save_mode_training_result, create_and_update_model_weight_records
from src.service.model_context_service import build_model, load_weights
from src.util import log
from src.util.constants import ModelClassLoaderTypes


logger = log.init_logger()


class DBModelExecutor(ModelExecutor):

    @abstractmethod
    def preprocessing(self, dataset: pd.DataFrame, get_flag: bool = False, skip_scaler: bool = False) -> pd.DataFrame:
        # IMPORTANT - LEAVE DEFINITION EMPTY AND DO NOT ADD EXCEPTION.
        return dataset

    @abstractmethod
    def shap_feature_explanation(self, *args, **kwargs):
        """
        Leave function definition empty
        """
        return

    def build_model(self):
        """
        Build the model based on the domain type.
        """
        logger.info("BuildModel from model class: %s, domain: %s",
                    self.name(), self.domain())
        batch_id = self.model_context.batch_id
        try:

            self.model = build_model(self.domain(), ModelClassLoaderTypes.DB)
            # load weight from DB
            weights = load_weights(self.domain())
            # Set the weights to the model
            self.model.set_weights(weights)
            logger.info("Model summary: %s", self.model.summary())
        except Exception as e:
            logger.error("error Run %s db build_model,batch_id: %s",
                         self.domain(), batch_id)
            logger.error(e)
            raise ValueError(
                f"Error run db build_model - {self.domain()}, batch_id: {batch_id}") from e

    def load_data(self):
        """
        Load the data based on the domain type and file path.
        To be overridden by subclasses.
        """
        batch_id = self.model_context.batch_id
        try:
            logger.info("get model feature records for batch: %s", batch_id)
            data: pd.DataFrame = get_model_feature_record(
                self.domain(), batch_id)
            # features = [list(row[1:]) for row in data]
            features = data.iloc[:, 1:]  # select all but the first column
            logger.info("Data size: %s", features.shape[0])
            return data, features
        except Exception as e:
            logger.error(
                "error Run %s db load_data,batch_id: %s",
                self.domain(), batch_id
            )
            logger.error(e)
            raise ValueError(
                f"Error run db predict - {self.domain()}, batch_id: {batch_id}"
            )

    def predict(self, data, features):
        logger.info("Run %s db prediction, data size: %s",
                    self.domain(), len(data))
        try:
            workflow_trace_id = self.model_context.workflow_trace_id

            # Extract the first column (payment_id)
            item_ids = data.iloc[:, 0]

            # Print the shape of the features array
            logger.info("Shape of features array before conversion: %s",
                        np.array(features).shape)

            # Convert features to a NumPy array and ensure the correct data type
            # Make predictions
            features_array = self.preprocessing(features)
            logger.info("Making predictions...")
            y_hat = self.model.predict(features_array)
            logger.info("Predictions complete. Storing results...")

            self.shap_feature_explanation(features_array, y_hat)

            n = len(features)
            logger.info("Sample size: %s", n)

            def get_pct(val: np.float64):
                return np.round((val if np.round(val) == 1 else 1 - val), decimals=6)

            values = [
                (np.round(y_hat[i][0], 6) * 100, get_pct(y_hat[i][0]) * 100, int(item_ids[i])) for i in range(n)
            ]
            sql_update_query = "UPDATE model_predict_data SET result={}, confidence_score={} WHERE item_id={};"
            queries = [sql_update_query.format(*v) for v in values]
            update_batches = math.ceil(len(values) / 1000)

            # Execute the batch insert
            start = 0
            batch_size = 1000
            for batch_idx in range(update_batches):
                end = start + batch_size
                logger.info("Executing batch update for Predict Data")
                total_records = DBConnection.execute_multiple_queries(
                    queries[start: end if end < len(
                        values) else (len(values) - 1)]
                )
                start = end
                logger.info("Total records updated: %s", total_records)
            return y_hat
        except Exception as e:
            logger.error("error Run %s db prediction, data size: %s",
                         self.domain(), len(data))
            logger.error(e)
            raise ValueError(
                f"Error run db predict - {self.domain()}, data size: {len(data)}")

    def run_training(self):
        """
        Load the data and traning, this only for db version
        """
        batch_id = self.model_context.batch_id
        workflow_trace_id = self.model_context.workflow_trace_id
        try:
            logger.info(
                "---> 1. build training %s Model, workflowTraceId: %s, batch_id: %s <---",
                self.domain(),
                workflow_trace_id,
                batch_id
            )
            self.build_model()
            logger.info(
                "2. get model feature records for batch: {0}".format(batch_id))
            data: pd.DataFrame = get_model_training_record(
                self.domain(), batch_id)
            # Check if data is retrieved successfully
            if data.empty:
                logger.info("No data found for domain: {0}, batch_id: {1}".format(
                    self.domain(), batch_id))
                create_workflow_model_process(
                    workflow_trace_id, 'OFL-C4', 'Fail')
                return 'fail', workflow_trace_id, None, 0, None

            # if we don't receive any feedback from the user
            logger.info("found model training records: {0} for batch: {1}".format(
                len(data), batch_id))
            # Log the columns retrieved and their count
            logger.info(f"Columns retrieved: {len(data.columns)}")

            features_array = data.iloc[:, 1:-3]
            model_input_data: pd.DataFrame = self.preprocessing(features_array)

            # get user feedback. if null, don't assume the value to be N.
            # we don't include rows for which feedback has not been provided
            # is_correct_req: pd.DataFrame = data.loc[data.iloc[:, -1].notnull(), :]
            # valid_feedback_mask = data.iloc[:, -2].notnull()
            # is_correct_req: pd.DataFrame = data.loc[valid_feedback_mask, data.columns[-2]

            # we skip online training.
            # since there was no feedback, we cannot train. we just evaluate to generate some metrics
            # and return that
            num_examples = model_input_data.shape[0]
            result_list = data.loc[:, data.columns[-3]].apply(
                lambda _: 0.0 if not _ or not _.strip() else np.round(np.float64(_) / 100)
            )
            eval_metrics = self.model.evaluate(
                model_input_data, result_list, return_dict=True, verbose=2)
            metrics = {"accuracy": eval_metrics["accuracy"]}
            loss = eval_metrics["loss"]

            # Save the model training result

            logger.info("save model training result: %s, num_examples: %s",
                        workflow_trace_id, num_examples)
            save_mode_training_result(workflow_trace_id, num_examples, metrics)
            weights_compressed = compress_weights(self.model.get_weights())
            logger.info("save model weight version records: %s",
                        workflow_trace_id)
            create_and_update_model_weight_records(
                workflow_trace_id, self.domain(), weights_compressed, self.domain())
            return 'success', workflow_trace_id, loss, num_examples, metrics
        except Exception as e:
            logger.error("error Run {0} db training, workflowTraceId: {1}, batch_id: {2}".format(
                self.domain(),
                workflow_trace_id,
                batch_id)
            )
            logger.error(e)
            raise ValueError(
                f"Error run db training - {self.domain()}, workflowTraceId: {workflow_trace_id}, batch_id: {batch_id}")

    def label(self, predictions):
        """
        Load the data based on the domain type and file path.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def fit(self, data, features, labels, predictions):
        batch_id = self.model_context.batch_id
        workflow_trace_id = self.model_context.workflow_trace_id
        try:
            logger.info("Run db fit, data size: {0}".format(len(data)))
            features_array = data[features].values
            logger.info("Final Features shape: {0}".format(
                features_array.shape))
            logger.info("Labels array shape: {0}".format(labels.shape))
            history = self.model.fit(
                features_array, labels, epochs=10, batch_size=32, validation_split=0.2)
            return self.model
        except Exception as e:
            logger.error(
                "error Run {0} db fit, workflowTraceId: {1}, batch_id: {2}".format(self.domain(), workflow_trace_id,
                                                                                   batch_id))
            raise ValueError(
                f"Error run db fit - {self.domain()}, workflowTraceId: {workflow_trace_id}, batch_id: {batch_id}")

    def evaluate(self, fit_weights, data, features, labels, additional_info):
        logger.info("---- client_evaluate-----")
        self.model.set_weights(fit_weights)
        features_array = data[features].values
        loss, accuracy = self.model.evaluate(features_array, labels)
        logger.info(f"Evaluation results - Loss: {loss}, Accuracy: {accuracy}")
        return loss, len(features_array), {"accuracy": accuracy}

    def generate_labels_array(self, is_correct_req, result_list, threshold):
        """
        Load the data based on the domain type and file path.
        To be overridden by subclasses.
        """
        raise NotImplementedError
