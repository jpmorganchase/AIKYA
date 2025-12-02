from src.ml_models.model.base_model import BaseModel
from src.util import log

import tensorflow as tf

logger = log.init_logger()

CREDIT_CARD_FRAUD_FEATURE_LIST = ['V1', 'V2', 'V3', 'V4', 'V5',
                                  'V6', 'V7', 'V8', 'V9', 'V10', 'V11', 'V12',
                                  'V13', 'V14', 'V15', 'V16', 'V17', 'V18',
                                  'V19', 'V20', 'V21', 'V22', 'V23', 'V24',
                                  'V25', 'V26', 'V27', 'V28', 'Amount']


class CreditCardModel(BaseModel):
    """
    Concrete model for the credit card domain.
    """

    def __init__(self):
        super().__init__()

    def create_model(self):
        """
        Create the credit card model.
        """
        self.model = create_credit_card_model()
        # For now, raise NotImplementedError to indicate this needs to be implemented
        # raise NotImplementedError("CreditCardModel create_model method is not implemented yet.")

    def compile_model(self):
        """
        Compile the credit card model.
        """
        # For now, raise NotImplementedError to indicate this needs to be implemented
        compile_credit_card_model(self.model)
        # raise NotImplementedError("CreditCardModel compile_model method is not implemented yet.")

    def model_name(self) -> str:
        return "CreditCardModel"

    def features(self):
        # TODO: REPLACE WITH ACTUAL COLUMNS
        # return ['time_step'] + [f'v{i}' for i in range(1, 29)]
        return CREDIT_CARD_FRAUD_FEATURE_LIST

    def name(self):
        return "Credit Card DB Executor"


def create_credit_card_model():
    """
    Create and return the credit card model.

    :return: Credit card model.
    """
    # For now, raise NotImplementedError to indicate this needs to be implemented
    logger.info("Creating model for credit card fraud domain")
    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(len(CREDIT_CARD_FRAUD_FEATURE_LIST),)),
            tf.keras.layers.Dense(16, activation='relu'),
            tf.keras.layers.Dense(8, activation='relu'),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])
    # raise NotImplementedError("create_credit_card_model function is not implemented yet.")
    return model


def compile_credit_card_model(model):
    """
    Compile the credit card model.

    :param model: The credit card model to be compiled.
    :raises InvalidDomainError: If the model is None.
    """
    # For now, raise NotImplementedError to indicate this needs to be implemented
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    # raise NotImplementedError("compile_credit_card_model function is not implemented yet.")
