import base64
import gzip
import json
import pickle

from src.util import log

logger = log.init_logger()


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
        weights_deserialized = base64.b64decode(weights_encoded)
        weights_compressed = base64.b64decode(weights_deserialized)

        if is_gzip_compressed(weights_compressed):
            logger.info("Data is gzip compressed. Proceeding with gzip decompression.")
            try:
                weights_serialized = gzip.decompress(weights_compressed)
                logger.info(f"Decompressed weights. Type: {type(weights_serialized)}, size: {len(weights_serialized)}")
            except OSError as e:
                logger.error(f"Error during gzip decompression: {e}")
                raise
        else:
            logger.info("Data is not gzip compressed. Skipping gzip decompression.")
            weights_serialized = weights_compressed

        # Step 3: Deserialize using pickle
        weights = pickle.loads(weights_serialized)
        logger.debug("Weights decompressed and deserialized successfully.")
        return weights

    except Exception as e:
        logger.error(f"Error during decompression: {e}")
        raise


def is_gzip_compressed(data):
    """
    Check if the data is in gzip format.

    Args:
        data (bytes): The data to check.

    Returns:
        bool: True if the data is gzip compressed, False otherwise.
    """
    return data[:2] == b'\x1f\x8b'


def format_metrics(metrics):
    """Format metrics into a readable string."""
    return "\n".join([f"{key}: {value}" for key, value in metrics.items()])


def weighted_metrics_average(metrics):
    accuracies = [num_examples * m["accuracy"] for num_examples, m in metrics]
    # Handle missing loss keys
    losses = [num_examples * m.get("loss", 0) for num_examples, m in metrics]
    examples = [num_examples for num_examples, _ in metrics]

    total_examples = sum(examples)
    avg_accuracy = sum(accuracies) / total_examples
    avg_loss = sum(losses) / total_examples if sum(losses) > 0 else None

    result = {"accuracy": avg_accuracy}
    if avg_loss is not None:
        result["loss"] = avg_loss
    return result
