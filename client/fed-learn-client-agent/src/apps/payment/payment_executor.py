import numpy as np
import math
from src.ml_models.executor.db_executor import DBModelExecutor
from src.util import log

logger = log.init_logger()


class PaymentExecutor(DBModelExecutor):
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
            "generate payment labels array, threshold: %s, is_correct_req size: %s, result size: %s",
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

    def name(self):
        return "Payment DB Executor"
