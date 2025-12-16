import pandas as pd
from src.util import log

logger = log.init_logger()


class DataLoader:
    def __init__(self, filepath, features):
        self.filepath = filepath
        self.features = features

    def load_data(self):
        data = pd.read_csv(self.filepath)
        return data

    def load_columns_data(self, columns):
        try:
            if columns:
                data = pd.read_csv(self.filepath, usecols=columns)
            else:
                data = pd.read_csv(self.filepath)
            return data
        except Exception as e:
            logger.error(f"Error loading {self.filepath} data: {e}")
            raise ValueError(f"Error loading data - {self.filepath}")

    def load_feature_data(self):
        try:
            data = pd.read_csv(self.filepath, usecols=self.features)
            data = data[self.features]
            return data
        except Exception as e:
            logger.error(f"Error load {self.filepath} data: {e}")
            raise ValueError(f"Error load data - {self.filepath}")

