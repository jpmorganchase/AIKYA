from src.ml_models.loader.model_loader import ModelLoader
from src.ml_models.model.base_model import BaseModel
from src.apps.creditcard.credit_card_model import CreditCardModel
from src.apps.payment_fraud.payment_fraud_model import PaymentFraudModel
from src.apps.payment.payment_model import PaymentModel
from src.util import log
from src.util.constants import DomainTypes

logger = log.init_logger()


class ClassModelLoader(ModelLoader):
    """
    Concrete strategy for loading models from Python classes.
    """

    def load_model(self, domain_type):
        """
        Load a model from a Python class based on the domain type.

        :param domain_type: The domain type for which the model is to be loaded.
        :return: Loaded model.
        """
        logger.info(f"Loading model for {domain_type} from Python Class")

        model_class = self.get_model_class(domain_type)
        model = model_class.get_model()
        if model is None:
            logger.error(f"Model for {domain_type} is None. Creating and compiling model.")
            model_class = self.get_model_class(domain_type)
            model_class.create_model()
            model_class.compile_model()
            model = model_class.get_model()
        logger.info(f"Successfully loaded model for {domain_type} from Python Class")
        return model

    def get_model_class(self, domain_type) -> BaseModel:
        """
        Get the appropriate model class based on the domain type.

        :param domain_type: The domain type for which to get the model class.
        :return: Instance of the appropriate model class.
        """
        if domain_type == DomainTypes.PAYMENT:
            return PaymentModel()
        elif domain_type == DomainTypes.CREDIT_CARD:
            return CreditCardModel()
        elif domain_type == DomainTypes.PAYMENT_FRAUD:
            return PaymentFraudModel()
        else:
            raise ValueError(f"Unknown domain type: {domain_type}")
