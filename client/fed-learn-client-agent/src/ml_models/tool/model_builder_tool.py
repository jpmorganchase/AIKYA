import base64
import gzip
import pickle
import random
import numpy as np
import tensorflow as tf
from keras.src.losses import BinaryCrossentropy, SparseCategoricalCrossentropy
from sklearn.model_selection import train_test_split
from src.util import log
from src.util.constants import DomainTypes
import math

logger = log.init_logger()


class Sequential(tf.keras.Sequential):
    pass


def capture_model_summary(model):
    # This function captures the model summary
    try:
        from io import StringIO
        stream = StringIO()
        model.summary(print_fn=lambda x: stream.write(f"{x}\n"))
        summary_str = stream.getvalue()
        stream.close()
        return summary_str
    except Exception as e:
        logger.error(f"Error capturing model summary: {e}")
        return "Error capturing model summary."


def load_model_from_json_string(model_json: str):
    try:
        # custom_objects = {'Sequential': Sequential}
        # model = tf.keras.models.model_from_json(model_json, custom_objects=custom_objects)
        custom_objects = {'Sequential': Sequential}
        model = tf.keras.models.model_from_json(model_json)
        # model = tf.keras.models.model_from_json(model_json)
        model_summary = capture_model_summary(model)
        logger.info("Model architecture loaded successfully.\nModel Summary:\n{0}".format(model_summary))
        return model
    except Exception as e:
        logger.error(f"Error loading model from JSON: {e}")
        raise


def compress_weights(weights):
    logger.info("Compressing weights...")
    weights_serialized = pickle.dumps(weights)
    weights_compressed = gzip.compress(weights_serialized)
    weights_encoded = base64.b64encode(weights_compressed).decode('utf-8')
    logger.info(f"Weights compressed and encoded: {weights_encoded[:100]}...")  # Log first 100 characters
    return weights_encoded


def decompress_weights(weights_encoded):
    logger.info(f"Decompressing weights. Input type: {type(weights_encoded)}, size: {len(weights_encoded)}")
    try:
        # Step 1: Decode from base64
        weights_compressed = base64.b64decode(weights_encoded)
        logger.debug(f"Decoded weights. Type: {type(weights_compressed)}, size: {len(weights_compressed)}")

        # Step 2: Decompress using gzip
        weights_serialized = gzip.decompress(weights_compressed)
        logger.info(f"Decompressed weights. Type: {type(weights_serialized)}, size: {len(weights_serialized)}")

        # Step 3: Deserialize using pickle
        weights = pickle.loads(weights_serialized)
        logger.debug("Weights decompressed and deserialized successfully.")
        return weights
    except Exception as e:
        logger.error(f"Error during decompression: {e}")
        raise


def client_evaluate(model, x_test, y_test):
    logger.info("client_evaluate")
    loss, accuracy = model.evaluate(x_test, y_test)
    return loss, len(x_test), {"accuracy": accuracy}


def client_evaluate_params(model, parameters, x_test, y_test):
    logger.info("client_evaluate with weights")
    model.set_weights(parameters)
    loss, accuracy = model.evaluate(x_test, y_test)
    return loss, len(x_test), {"accuracy": accuracy}


def compile_and_train_model(model, domain: str, features_array, labels_array,
                            test_size=0.2, random_state=42, epochs=15,
                            batch_size=10):
    """
    Compile, train, and evaluate the model.

    :param model: The model to be trained and evaluated.
    :param domain: The model to be trained and evaluated.
    :param features_array: NumPy array of features.
    :param labels_array: NumPy array of labels.
    :param test_size: Fraction of the data to be used as test set.
    :param random_state: Seed used by the random number generator.
    :param epochs: Number of epochs to train the model.
    :param batch_size: Batch size for training the model.
    :return: loss, accuracy, metrics, num_examples
    """
    # Split the data into training and testing sets
    try:
        if len(features_array) > 10:
            x_train, x_test, y_train, y_test = train_test_split(features_array, labels_array, test_size=test_size,
                                                                random_state=random_state)
        else:
            x_train, x_test, y_train, y_test = (features_array, features_array, labels_array, labels_array)
        # if domain in (DomainTypes.CREDIT_CARD, DomainTypes.PAYMENT_FRAUD):
        #     model.compile(
        #         optimizer='adam',
        #         loss="binary_crossentropy",
        #         metrics=['accuracy']
        #     )
        # else:
        #     model.compile(
        #         optimizer='adam',
        #         loss=SparseCategoricalCrossentropy(),
        #         metrics=['accuracy']
        #     )
        # Train the model
        logger.info("Training the model")
        model.fit(x_train, y_train, epochs=epochs, batch_size=batch_size, verbose=2)
        # Evaluate the model
        logger.info("Evaluating the model")
        loss, accuracy = model.evaluate(x_test, y_test)
        num_examples = len(x_train)
        metrics = {"accuracy": accuracy}

        logger.info(f"Loss: {loss}")
        logger.info(f"Number of Test Examples: {num_examples}")
        logger.info(f"Metrics: {metrics}")

        return loss, accuracy, metrics, num_examples
    except Exception as e:
        logger.error(f"Error compile and train model: {e}")
        raise


def generate_labels_array(is_correct_req, result_list, threshold=73.0):
    """
    Generate labels array based on is_correct_req and result_list with a given threshold.

    :param is_correct_req: List of strings indicating whether the request is correct ('Y' or 'N').
    :param result_list: List of floats representing some result values.
    :param threshold: Float value to use as the threshold for labeling.
    :return: NumPy array of labels.
    TODO: Review THIS LOGIC
    """
    y = []
    for i in range(len(result_list)):
        if is_correct_req[i] == "Y":
            if result_list[i] > threshold:
                y.append(0)
            else:
                y.append(1)
        else:
            if result_list[i] > threshold:
                y.append(1)
            else:
                y.append(0)
    labels_array = np.array(y, dtype=np.int32)
    return labels_array


def simulate_feedback_is_correct(predict_data, threshold=50.0):
    """
    Assign the is_correct_req flag to each prediction result based on a threshold.

    Args:
        predict_data (list): A list of dictionaries containing prediction results.
        threshold (float): The threshold to determine if the prediction is correct.

    Returns:
        list: A list of dictionaries with prediction results and is_correct_req flag.
    """
    for entry in predict_data:
        result = entry['result']
        entry['is_correct_req'] = 'Y' if result >= threshold else 'N'
    return predict_data


def simulate_feedback_is_correct_random(predict_data):
    """
    Assign the is_correct_req flag to each prediction result based on a threshold.

    Args:
        predict_data (list): A list of dictionaries containing prediction results.
        threshold (float): The threshold to determine if the prediction is correct.

    Returns:
        list: A list of dictionaries with prediction results and is_correct_req flag.
    """
    for entry in predict_data:
        # Randomly assign 'Y' or 'N' to is_correct_req
        entry['is_correct_req'] = random.choice(['Y', 'N'])
    return predict_data
