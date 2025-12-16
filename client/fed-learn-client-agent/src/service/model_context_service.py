import traceback

import numpy as np

from src.common.parameter import parameters_to_ndarrays
from src.ml_models.loader.class_model_loader import ClassModelLoader
from src.ml_models.loader.db_model_loader import DBModelLoader
from src.ml_models.loader.model_load_context import ModelLoaderContext
from src.ml_models.model.base_model import InvalidDomainError, BaseModel
from src.ml_models.tool.model_builder_tool import load_model_from_json_string, decompress_weights
from src.repository.model.model_track_repository import get_model_track_record
from src.util import log
from src.util.constants import ModelClassLoaderTypes

logger = log.init_logger()


class ModelContextService:
    """ Class for machine learning service """

    def __init__(self):
        """
        Initialize the ModelContextService.
        """
        pass


def load_weights(domain_type):
    """
    Load and process weights for the given domain type.

    :param domain_type: The domain type for which to load the weights.
    :return: Processed weights as NumPy arrays.
    """
    # Retrieve the model track record
    try:
        model_track_record = get_model_track_record(domain_type)
        local_model_weights = model_track_record[2]
        global_model_weights = model_track_record[4]

        if global_model_weights is None:
            logger.info("global_model_weights is empty, use default local model weight")
            weights_encoded = local_model_weights
        else:
            logger.info("found global_model_weights")
            weights_encoded = global_model_weights

        logger.info("Decompress and decode weights for domain '{0}'.".format(domain_type))
        weights = decompress_weights(weights_encoded)

        if global_model_weights is not None:
            logger.info("Converting aggregate global model weights to ndarrays")
            if isinstance(weights, np.ndarray) or (
                    isinstance(weights, list) and all(isinstance(w, np.ndarray) for w in weights)):
                logger.info("Weights are already in NDArray format.")
            else:
                logger.info("Converting aggregate global model weights to ndarrays")
                weights = parameters_to_ndarrays(weights)

        return weights
    except Exception as e:
        # Capture the detailed stack trace
        error_details = traceback.format_exc()
        logger.error("Error load_weights {0} from db: {1}".format(domain_type, e))
        logger.error("Detailed error: {}".format(error_details))
        raise ValueError(f"Error db load_weights - {domain_type}") from e


def get_model_weights(model_json: str):
    try:
        if not model_json:
            raise ValueError("Model JSON is empty")
        model = load_model_from_json_string(model_json)
        weights = model.get_weights()
        logger.info("Model weights retrieved successfully.")
        return weights
    except Exception as e:
        logger.error(f"Error getting model weights: {e}")
        raise


def build_model_from_class(domain_type):
    """
    Build a model based on the domain type by selecting the appropriate strategy.

    :param domain_type: The domain type for which the model is to be built.
    :return: Built model.
    """
    logger.info(f"Loading model for: {domain_type}")
    model_loader_type = ClassModelLoader()
    logger.info("Model loader config type for Domain: {0}, Load from: {1}.".format(domain_type, model_loader_type))
    context = ModelLoaderContext(model_loader_type)
    model = context.load_model(domain_type)
    logger.info(f"Successfully loaded model: {domain_type}")
    return model


def get_model_context_class(domain_type) -> BaseModel:
    """
    Build a model based on the domain type by selecting the appropriate strategy.

    :param domain_type: The domain type for which the model is to be built.
    :return: Built model.
    """
    logger.info(f"Loading model for: {domain_type}")
    model_loader_type = ClassModelLoader()
    logger.info("Model loader config type for Domain: {0}, Load from: {1}.".format(domain_type, model_loader_type))
    context = ModelLoaderContext(model_loader_type)
    model_class = context.get_model_class(domain_type)
    logger.info(f"Successfully loaded model: {model_class.model_name()}")
    return model_class


def generate_model_json(domain=None):
    try:
        model = build_model_from_class(domain)
        # Convert the model to JSON
        model_json = model.to_json()
        return model_json
    except InvalidDomainError as e:
        logger.error(f"Error generating model JSON: {e}")
        raise


def build_model(domain_type, source):
    """
    Build a model based on the domain type by selecting the appropriate strategy.

    :param domain_type: The domain type for which the model is to be built.
    :param source: db or class.
    :return: Built model.
    """
    logger.info(f"Loading model for: {domain_type}")
    model_loader_type = None

    if source == ModelClassLoaderTypes.DB:
        model_loader_type = DBModelLoader()
    elif source == ModelClassLoaderTypes.CLASS:
        model_loader_type = ClassModelLoader()

    logger.info("Model loader config type for Domain: {0}, Load from: {1}.".format(domain_type, model_loader_type))

    context = ModelLoaderContext(model_loader_type)
    model = context.load_model(domain_type)

    logger.info(f"Successfully loaded model: {domain_type}")
    return model
