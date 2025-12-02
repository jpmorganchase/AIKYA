from src.ml_models.model.base_model import BaseModel
from src.util import log
import math
import keras

logger = log.init_logger()

PAYMENT_FRAUD_DATA_FEATURE_LIST = [
    "DEBITOR_AMOUNT", "DEBITOR_GEO_LATITUDE", "DEBITOR_GEO_LONGITUDE", "DEBITOR_TOWER_LATITUDE",
    "DEBITOR_TOWER_LONGITUDE",
    "CREDITOR_GEO_LATITUDE", "CREDITOR_GEO_LONGITUDE", "CREDITOR_TOWER_LATITUDE", "CREDITOR_TOWER_LONGITUDE",
    "DEBITOR_ACCOUNT_CREATE_TIMESTAMP", "CREDITOR_ACCOUNT_CREATE_TIMESTAMP",
    "DEBITOR_BIRTH_YEAR", "DEBITOR_BIRTH_MONTH", "DEBITOR_BIRTH_DAY", "DEBITOR_BIRTH_YEAR", "DEBITOR_BIRTH_MONTH",
    "DEBITOR_BIRTH_DAY", "PAYMENT_INIT_TIMESTAMP"
]

PAYMENT_FRAUD_MODEL_FEATURE_LIST = [
    'DEBITOR_PHY_AND_TOWER_DISTANCE',
    'CREDITOR_PHY_AND_TOWER_DISTANCE',
    'DEBITOR_AMOUNT',
    'DEBITOR_ACCOUNT_AGE',
    'CREDITOR_ACCOUNT_AGE'
]

PAYMENT_FRAUD_FLAG = "FRAUD_FLAG"


class PaymentFraudModel(BaseModel):
    """
    Concrete model for the credit card domain.
    """

    def __init__(self):
        super().__init__()

    def create_model(self):
        """
        Create the payment fraud model
        """
        logger.info("Creating model for payment fraud domain")
        self.model = keras.Sequential(name=self.model_name())
        self.model.add(keras.layers.Input(
            shape=(len(PAYMENT_FRAUD_MODEL_FEATURE_LIST),)))
        self.model.add(keras.layers.Dense(
            math.ceil(len(PAYMENT_FRAUD_MODEL_FEATURE_LIST) * 4), activation='relu'))
        self.model.add(keras.layers.Dense(
            math.ceil(len(PAYMENT_FRAUD_MODEL_FEATURE_LIST) * 2), activation='relu'))
        self.model.add(keras.layers.Dense(1, activation='sigmoid'))

    def compile_model(self):
        """
        Compile the payment fraud model
        """
        # For now, raise NotImplementedError to indicate this needs to be implemented
        logger.info(f"Compiling {self.__class__.__name__} model")
        self.model.compile(
            optimizer=keras.optimizers.Adam(learning_rate=0.0001),
            loss='binary_crossentropy',
            metrics=['accuracy', "Precision", "Recall"]
        )

    def model_name(self) -> str:
        return "PaymentFraudModel"

    def features(self):
        return PAYMENT_FRAUD_DATA_FEATURE_LIST

    def name(self):
        return "Payment Fraud Model"
