from src.apps.creditcard.credit_card_simulation_executor import CreditCardSimulationExecutor
from src.apps.payment.payment_simulation_executor import PaymentSimulationExecutor
from src.apps.payment_fraud.payment_fraud_simulation_executor import PaymentFraudSimulationExecutor
from src.ml_models.provider.model_provider import ModelExecutorProvider
from src.util.constants import DomainTypes
from src.util import log

logger = log.init_logger()


class SimulationModelExecutorProvider(ModelExecutorProvider):
    def create_executor(self, model_context):
        domain = model_context.domain_type
        try:
            logger.info("create {0} simulation executor".format(domain))
            if domain == DomainTypes.PAYMENT:
                return PaymentSimulationExecutor(model_context)
            elif domain == DomainTypes.CREDIT_CARD:
                return CreditCardSimulationExecutor(model_context)
            elif domain == DomainTypes.PAYMENT_FRAUD:
                return PaymentFraudSimulationExecutor(model_context)
            else:
                raise ValueError(f"Unknown domain type: {model_context.domain_type}")
        except Exception as e:
            logger.error(f"Error create {domain} SimulationModelExecutor: {e}")
            raise ValueError(f"Error create SimulationModelExecutor - {domain}")

    def name(self):
        return "Simulation Model Executor Provider"
