from abc import abstractmethod, ABC


class ModelExecutorProvider(ABC):
    @abstractmethod
    def create_executor(self, model_context):
        pass

    @abstractmethod
    def name(self):
        pass

