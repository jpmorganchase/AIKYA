from src.ml_models.loader.model_loader import ModelLoader
from src.ml_models.model.base_model import BaseModel

from src.util import log

logger = log.init_logger()


class ModelLoaderContext:
    """
    Context class for using a model loading selection.
    """

    def __init__(self, loader: ModelLoader):
        """
        Initialize with a specific loader.

        :param loader: The loader to use for loading models.
        """
        self._loader = loader

    def set_loader(self, loader: ModelLoader):
        """
        Set a different loader for loading models.

        :param loader: The new loader to use.
        """
        self._loader = loader

    def load_model(self, domain_type):
        """
        Load a model using the current loader.

        :param domain_type: The domain type for which the model is to be loaded.
        :return: Loaded model.
        """
        logger.info("ModelLoaderContext load_model for {}".format(domain_type))
        return self._loader.load_model(domain_type)

    def get_model_class(self, domain_type) -> BaseModel:
        """
        Load a model using the current loader.

        :param domain_type: The domain type for which the model is to be loaded.
        :return: Loaded model.
        """
        logger.info("ModelLoaderContext get_model_class for {}".format(domain_type))
        return self._loader.get_model_class(domain_type)