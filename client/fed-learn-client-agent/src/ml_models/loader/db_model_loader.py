from src.ml_models.model.base_model import BaseModel
from src.util import log

logger = log.init_logger()
from src.ml_models.loader.model_loader import ModelLoader
from src.repository.model.model_track_repository import get_model_track_record
from src.ml_models.tool.model_builder_tool import load_model_from_json_string


class DBModelLoader(ModelLoader):
    """
    Concrete loader class for loading models from database.
    """

    def load_model(self, domain_type):
        """
        Load a model from a Python class based on the domain type.

        :param domain_type: The domain type for which the model is to be loaded.
        :return: Loaded model.
        """
        logger.info(f"Loading model for {domain_type} from DB")
        model_track_record = get_model_track_record(domain_type)
        model = load_model_from_json_string(model_track_record[0])
        logger.info(f"Successfully loaded model for {domain_type} from DB")
        return model

    def get_model_class(self, domain_type) -> BaseModel:
        """
        This method should not be used for DBModelLoader.

        :param domain_type: The domain type for which to get the model class.
        :return: Raises NotImplementedError.
        """
        raise NotImplementedError("DBModelLoader does not use model classes.")
