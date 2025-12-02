import logging
import random
from decimal import Decimal, InvalidOperation


def get_random_id():
    return random.randint(1000, 9999999999)


def convert_to_decimal_value(input_value, precision):
    try:
        # Convert the input to Decimal
        result = Decimal(input_value)
        # Quantize the Decimal to the specified precision
        result = result.quantize(Decimal(precision))
        return result
    except InvalidOperation:
        # Handle the case where input_value is not a valid number
        logging.info("Invalid input for decimal conversion:", input_value)
        return None
