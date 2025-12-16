from src.common.parameter import ndarrays_to_parameters, serialize_parameters
from src.entities.model_simulation_request import ClientData, Metrics
from src.service.model_context_service import get_model_context_class
from src.util import log

logger = log.init_logger()


class ModelExecutor:
    def __init__(self, model_context):
        self.model_context = model_context
        self.model = None
        self.result = None

    # def run(self):
    #     """
    #     The template method that defines the steps of the algorithm.
    #     """
    #     logger.info("---> 1. Loading {0} Model <---".format(self.domain()))
    #     self.build_model()
    #     logger.info("---> 2. Start load {0} data from: {1} <---".format(self.domain(), self.model_context.file_path))
    #     data, features = self.load_data()
    #     size = len(data)
    #     logger.info("---> 2. Complete load {0} data from: {1} <---".format(self.domain(), self.model_context.file_path))
    #     logger.info("---> 3. Start run {0} prediction, data size: {1} <---".format(self.domain(), size))
    #     predictions = self.predict(data, features)
    #     predict_result_size = len(predictions)
    #     logger.info(
    #         "---> 3. Complete run {0} prediction, data size: {1}, prediction size: {2} <---".format(self.domain(), size,
    #                                                                                                 predict_result_size))
    #     logger.info("---> 4. Start load {0} labels <---".format(self.domain()))
    #     labels = self.label(predictions)
    #     logger.info("---> 4. Complete load {0} labels <---".format(self.domain()))
    #     logger.info("---> 5. Start run {0} fit. data size: {1}, prediction size: {2} <---".format(self.domain(), size,
    #                                                                                               predict_result_size))
    #     fit_weights, features_train_length, additional_info = self.fit(data, features, labels, predictions)
    #     logger.info("---> 5. Complete run {0} fit. features train length: {1} <---".format(self.domain(),
    #                                                                                        features_train_length))
    #     logger.info("---> 5. Start run {0} evaluate. <---".format(self.domain()))
    #     loss, num_examples, metrics = self.evaluate(fit_weights, data, features, labels, additional_info)
    #     accuracy = metrics['accuracy']
    #     logger.info(
    #         "---> 5. Complete run {0} evaluate, num_examples: {1}. loss: {2}, accuracy: {3}<---".format(self.domain(),
    #                                                                                                     num_examples,
    #                                                                                                     loss, accuracy))
    #     # Construct the result as ClientData
    #     ser_parameters = ndarrays_to_parameters(fit_weights)
    #     ser_parameters_str = serialize_parameters(ser_parameters)
    #     metrics = Metrics(loss=loss, accuracy=accuracy)
    #     # Construct the result as ClientData
    #     self.result = ClientData(
    #         parameters=ser_parameters_str,  # Get model weights as parameters
    #         metrics=metrics,
    #         num_examples=num_examples  # Ensure num_examples is provided
    #     )
    #     return self.result

    def run_predict(self):
        """
        The template method that defines the steps of the algorithm.
        """
        logger.info(
            "---> 1. Loading {0} Model by {1} <---".format(self.domain(), self.name())
        )
        self.build_model()
        logger.info("---> 2. Start load {0} data <---".format(self.domain()))
        data, features = self.load_data()
        # apply feature data transformation here
        size = len(data)
        logger.info("---> 2. Complete load {0} data <---".format(self.domain()))
        logger.info(
            "---> 3. Start run {0} prediction, data size: {1} <---".format(
                self.domain(), size
            )
        )
        predictions = self.predict(data, features)
        return predictions

    def run_training(self):
        """
        Load the data based on the domain type and file path.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def build_model(self):
        """
        Build the model based on the domain type.
        """
        model_class = get_model_context_class(self.model_context.domain_type)
        model_class.create_model()
        model_class.compile_model()
        self.model = model_class.get_model()
        logger.info("Model summary: {0}".format(self.model.summary()))

    def load_data(self):
        """
        Load the data based on the domain type and file path.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def predict(self, data, features):
        """
        Predict the outcomes using the model and data.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def label(self, predictions):
        """
        Generate labels based on predictions.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def fit(self, data, features, labels, predictions):
        """
        Fit the model using the data, labels, and predictions.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def evaluate(self, fit_weights, data, features, labels, additional_info):
        """
        Evaluate the model.
        To be overridden by subclasses.
        """
        raise NotImplementedError

    def domain(self):
        return self.model_context.domain_type

    def name(self):
        """
        Get the name of the model executor.
        To be overridden by subclasses if needed.
        :return: Name of the model executor.
        """
        pass
