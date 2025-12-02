from abc import ABC, abstractmethod


class BaseModel(ABC):
    """
    Base model interface for all domain-specific models.
    """

    def __init__(self):
        self.model = None

    @abstractmethod
    def create_model(self):
        """
        Create the specific model for the domain.
        """
        pass

    @abstractmethod
    def compile_model(self):
        """
        Compile the specific model for the domain.
        """
        pass

    def get_model(self):
        """
        Get the specific model for the domain.

        :return: Domain-specific model.
        """
        if not hasattr(self, 'model'):
            self.create_model()
        return self.model

    @abstractmethod
    def model_name(self) -> str:
        """
        Get the name of the specific model for the domain.

        :return: Name of the domain-specific model.
        """
        pass

    @abstractmethod
    def features(self):
        """
        Define the features for the specific model.
        """
        pass


class InvalidDomainError(Exception):
    pass
