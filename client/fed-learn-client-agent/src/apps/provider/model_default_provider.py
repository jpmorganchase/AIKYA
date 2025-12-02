from src.apps.creditcard.creditcard_executor import CreditCardExecutor
from src.apps.payment.payment_executor import PaymentExecutor
from src.apps.payment_fraud.payment_fraud_executor import PaymentFraudExecutor
from src.ml_models.provider.model_provider import ModelExecutorProvider
from src.util.constants import DomainTypes
from src.util import log

logger = log.init_logger()


class DBModelExecutorProvider(ModelExecutorProvider):
    def create_executor(self, model_context):
        domain = model_context.domain_type
        try:
            if model_context.domain_type == DomainTypes.PAYMENT:
                return PaymentExecutor(model_context)
            elif model_context.domain_type == DomainTypes.CREDIT_CARD:
                return CreditCardExecutor(model_context)
            elif model_context.domain_type == DomainTypes.PAYMENT_FRAUD:
                return PaymentFraudExecutor(model_context)
            else:
                raise ValueError(f"Unknown domain type: {model_context.domain_type}")
        except Exception as e:
            logger.error(f"Error create {domain} SimulationModelExecutor: {e}")
            raise ValueError(f"Error create DBModelExecutor - {domain}")

    def name(self):
        return "DB Model Executor Provider"
