from src.ml_models.executor.model_executor import ModelExecutor
from src.service.model_context_service import get_model_context_class
from src.service.simulation.data_loader import DataLoader
from src.util import log

logger = log.init_logger()


class SimulationModelExecutor(ModelExecutor):

    def build_model(self):
        """
        Build the model based on the domain type.
        """
        logger.info("BuildModel from model class: {0}, domain: {1}".format(self.name(), self.domain()))
        model_class = get_model_context_class(self.model_context.domain_type)
        model_class.create_model()
        model_class.compile_model()
        self.model = model_class.get_model()
        logger.info("Model summary: {0}".format(self.model.summary()))

    def load_data(self):
        features = get_model_context_class(self.model_context.domain_type).features()
        data_loader = DataLoader(self.model_context.file_path, features)
        data = data_loader.load_feature_data()
        logger.info("Data size: {0}".format(len(data)))
        return data, features

    def predict(self, data, features):
        logger.info("Run simulation prediction, data size: {0}".format(len(data)))
        features_array = data[features].values
        logger.info("Shape of features array: {0}".format(features_array.shape))
        predictions = self.model.predict(features_array)
        return predictions

    def fit(self, data, features, labels, predictions):
        logger.info("Run simulation fit, data size: {0}".format(len(data)))
        features_array = data[features].values
        logger.info("Final Features shape: {0}".format(features_array.shape))
        logger.info("Labels array shape: {0}".format(labels.shape))
        history = self.model.fit(features_array, labels, epochs=10, batch_size=32, validation_split=0.2)
        return self.model

    def evaluate(self, fit_weights, data, features, labels, additional_info):
        print(f"---- client_evaluate-----")
        self.model.set_weights(fit_weights)
        features_array = data[features].values
        loss, accuracy = self.model.evaluate(features_array, labels)
        logger.info(f"Evaluation results - Loss: {loss}, Accuracy: {accuracy}")
        return loss, len(features_array), {"accuracy": accuracy}
