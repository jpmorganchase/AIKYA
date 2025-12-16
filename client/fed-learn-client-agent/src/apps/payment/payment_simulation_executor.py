from src.ml_models.executor.simulation_executor import SimulationModelExecutor
from src.ml_models.tool.model_builder_tool import generate_labels_array, simulate_feedback_is_correct, \
    simulate_feedback_is_correct_random
from src.service.model_context_service import get_model_context_class
from src.service.simulation.data_loader import DataLoader
from src.util import log

logger = log.init_logger()


class PaymentSimulationExecutor(SimulationModelExecutor):

    def load_data(self):
        try:
            features = get_model_context_class(self.domain()).features()
            logger.info(f"Features to be loaded: {features}")
            print(f"Features to be loaded: {features}")  # Print the features list to the console
            data_loader = DataLoader(self.model_context.file_path, features)
            data = data_loader.load_columns_data(features)
            logger.info("Payment Data size: {0}".format(len(data)))
            return data, features
        except Exception as e:
            logger.error(f"Error load {self.domain()} data: {e}")
            raise ValueError(f"Error load data - {self.domain()}, path: {self.model_context.file_path}")

    def predict(self, data, features):
        logger.info("Run {0} simulation prediction, data size: {1}".format(self.domain(), len(data)))
        try:
            features_array = data[features].values
            logger.info("Shape of features array: {0}".format(features_array.shape))
            predictions = self.model.predict(features_array)
            return predictions
        except Exception as e:
            logger.error("error Run {0} simulation prediction, data size: {1}".format(self.domain(), len(data)))
            raise ValueError(f"Error run simulation predict - {self.domain()}, data size: {len(data)}")

    def label(self, predictions):
        """
        we need is_correct_req 'Y' or 'N' which matches UI feedback flag
        result - %: matches current prediction result use in e2e
        """
        try:
            logger.info("Run {0} simulation label, predictions size: {1}".format(self.domain(), len(predictions)))
            predict_data = [{"result": float(100.0 * pred[0])} for pred in predictions]
            predict_data = self.simulate_feedback_is_correct(predict_data, 60.0)
            is_correct_req = [entry['is_correct_req'] for entry in predict_data]
            result_list = [entry['result'] for entry in predict_data]
            # TODO: Review THIS LOGIC
            threshold = 73.0
            labels_array = self.generate_labels_array(is_correct_req, result_list, threshold)
            return labels_array
        except Exception as e:
            logger.error("error Run {0} label".format(self.domain()))
            raise ValueError(f"Error run label - {self.domain()}")

    def fit(self, data, features, labels, predictions):
        try:
            features_array = data[features].values
            logger.info("Final Features shape: {0}".format(features_array.shape))
            logger.info("Labels array shape: {0}".format(labels.shape))
            self.model.fit(features_array, labels, epochs=10, batch_size=32, validation_split=0.2)
            return self.model.get_weights(), len(features_array), {}
        except Exception as e:
            logger.error("error Run fit {0} ".format(self.domain()))
            raise ValueError(f"Error run fit - {self.domain()}")

    def evaluate(self, fit_weights, data, features, labels, additional_info):
        try:
            print(f"---- client_evaluate-----")
            self.model.set_weights(fit_weights)
            features_array = data[features].values
            loss, accuracy = self.model.evaluate(features_array, labels)
            logger.info(f"Evaluation results - Loss: {loss}, Accuracy: {accuracy}")
            return loss, len(features_array), {"accuracy": accuracy}
        except Exception as e:
            logger.error("error Run evaluate {0} ".format(self.domain()))
            raise ValueError(f"Error run evaluate - {self.domain()}")

    def generate_labels_array(self, is_correct_req, result_list, threshold=73.0):
        return generate_labels_array(is_correct_req, result_list, threshold)

    def simulate_feedback_is_correct(self, predict_data, threshold=50.0):
        # return simulate_feedback_is_correct(predict_data, threshold)
        return simulate_feedback_is_correct_random(predict_data)

