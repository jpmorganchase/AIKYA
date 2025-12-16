import numpy as np

from src.ml_models.executor.db_executor import DBModelExecutor
from src.service.model_context_service import get_model_context_class
from src.service.simulation.data_loader import DataLoader
from src.util import log
import math

logger = log.init_logger()


class CreditCardExecutor(DBModelExecutor):
    """
       TODO: Review and overwrite this class. All current logic is similar to the default.
       If there is overlap, remove the related functions and use superclass logic.
    """

    def generate_labels_array(self, is_correct_req, result_list, threshold=73.0):
        """
        Generate labels array based on is_correct_req and result_list with a given threshold.

        :param is_correct_req: List of strings indicating whether the request is correct ('Y' or 'N').
        :param result_list: List of floats representing some result values.
        :param threshold: Float value to use as the threshold for labeling.
        :return: NumPy array of labels.
        TODO: Review THIS LOGIC
        """
        logger.info(
            "generate payment labels array, threshold: %s, is_correct_req size: %s, size: %s",
            threshold, len(is_correct_req), len(result_list)
        )
        y = []
        for i in range(len(result_list)):
            if np.isnan(result_list[i]) or math.isnan(result_list[i]):
                continue

            match is_correct_req[i].upper():
                case "Y":
                    y.append(result_list[i])
                case "N":
                    flip = math.floor(result_list[i]) \
                        if result_list[i] < threshold \
                        else math.ceil(result_list[i])
                    y.append(1 - flip)
        labels_array = np.array(y, dtype=np.int64)
        return labels_array

    # def simulate_feedback_is_correct(self, predict_data, threshold=50.0):
    #     """
    #     Assign the is_correct_req flag to each prediction result based on a threshold.
    #
    #     Args:
    #         predict_data (list): A list of dictionaries containing prediction results.
    #         threshold (float): The threshold to determine if the prediction is correct.
    #
    #     Returns:
    #         list: A list of dictionaries with prediction results and is_correct_req flag.
    #     """
    #     # TODO: Review and Overwrite THIS LOGIC if need
    #     for entry in predict_data:
    #         result = entry['result']
    #         entry['is_correct_req'] = 'Y' if result >= threshold else 'N'
    #     return predict_data
    def name(self):
        return "Credit Card DB Executor"
