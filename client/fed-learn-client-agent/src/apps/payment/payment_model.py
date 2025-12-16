import tensorflow as tf
from src.util import log

logger = log.init_logger()
from src.ml_models.model.base_model import BaseModel, InvalidDomainError

PAYMENT_FEATURE_LIST = [
    'crdtr_account_is_sanction',
    'crdtr_country_is_sanction',
    'crdtr_bank_is_sanction',
    'crdtr_amount_is_flagged',
    'crdtr_diff_threshold',
    'crdtr_std_dev_th_hist',
    'crdtr_email_reputation',
    'crdtr_is_phone_active_1m',
    'crdtr_hist_txns_flag_per',
    'crdtr_age',
    'crdtr_country_encode',
    'dbtr_account_is_sanction',
    'dbtr_country_is_sanction',
    'dbtr_bank_is_sanction',
    'dbtr_amount_is_flagged',
    'dbtr_diff_threshold',
    'dbtr_std_dev_th_hist',
    'dbtr_email_reputation',
    'dbtr_is_phone_active_1m',
    'dbtr_hist_txns_flag_per',
    'dbtr_age',
    'dbtr_country_encode',
    'is_ccy_sanctioned',
    'num_txns_same_dir_1y',
    'num_txns_rev_dir_1y',
    'avg_gap_txns_same_dir_1y',
    'avg_gap_txns_rev_dir_1y'
]


class PaymentModel(BaseModel):
    """
    Concrete model for the payment domain.
    """

    def __init__(self):
        super().__init__()

    def create_model(self):
        """
        Create the payment model.
        """
        self.model = create_payment_model()

    def compile_model(self):
        """
        Compile the payment model.
        """
        compile_payment_model(self.model)

    def model_name(self) -> str:
        return "PaymentModel"

    def features(self):
        return PAYMENT_FEATURE_LIST


def create_payment_model():
    """
    Create and return the payment model.

    :return: Payment model.
    """
    logger.info("Creating model for payment domain")
    model = tf.keras.Sequential([
        tf.keras.layers.InputLayer(input_shape=(len(PAYMENT_FEATURE_LIST),)),
        tf.keras.layers.Dense(32, activation='relu'),
        tf.keras.layers.Dense(64, activation='relu'),
        tf.keras.layers.Dense(2, activation="softmax")
    ])
    return model


def compile_payment_model(model):
    """
    Compile the payment model.

    :param model: The payment model to be compiled.
    :raises InvalidDomainError: If the model is None.
    """
    if model is None:
        raise InvalidDomainError("Payment model is not recognized. Please provide a valid model.")

    logger.info("Compiling model for payment domain")
    model.compile(
        optimizer='adam',
        loss=tf.keras.losses.SparseCategoricalCrossentropy(),
        metrics=['accuracy']
    )
