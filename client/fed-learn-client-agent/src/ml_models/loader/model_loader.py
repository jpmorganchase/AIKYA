from abc import ABC, abstractmethod

from src.ml_models.model.base_model import BaseModel


class ModelLoader(ABC):
    """
     interface for loading models. All concrete loader should implement this interface.
    """

    @abstractmethod
    def load_model(self, domain_type):
        """
        Load a model based on the domain type.

        :param domain_type: The domain type for which the model is to be loaded.
        :return: Loaded model.
        """
        pass

    @abstractmethod
    def get_model_class(self, domain_type) -> BaseModel:
        """
        Get the appropriate model class based on the domain type.

        :param domain_type: The domain type for which to get the model class.
        :return: Instance of the appropriate model class.
        """
        pass
