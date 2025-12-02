import tensorflow as tf
from tensorflow import keras
from src.util import log

logger = log.init_logger()
import logging
# Configure logging
logging.basicConfig(level=logging.INFO)


def print_tensorflow_version():
    logging.info("TensorFlow version: {}".format(tf.__version__))
    logging.info("Keras version: {}".format(keras.__version__))
