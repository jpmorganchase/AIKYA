import csv
from datetime import datetime
from decimal import Decimal, InvalidOperation

from src import instance_pool, configuration
from src.repository.process.seeds_repository import get_table_id_field
from src.service.file.csv_file_handler import CsvFileHandler
import logging as logger
from src.repository.base_repository import execute_batch_insert, get_table_schema


class DefaultCsvFileHandler(CsvFileHandler):
    """
    DefaultCsvFileHandler

    This class provides automation for converting CSV data to database rows based on mapping information.
    It performs a direct 1-1 data conversion with proper type conversion and adds missing columns by assigning
    default values.

    Configuration:
    Define your configurations in the app environment:
    - app.file.validate.{domain}.db.table: The database table to insert data into.
    - app.file.validate.{domain}.csv_table_mapping: The CSV to database column mapping file.
    - app.file.validate.{domain}.file_handler: The custom CSV file handler, if needed.

    Example Configuration:
    - app.file.validate.payment.db.table=domain_payment_data
    - app.file.validate.payment.csv_table_mapping=conf/payment_csv_table_columns_mapping.csv
    - app.file.validate.payment.file_handler=app/payment/payment_csv_file_handler.py

    If you need a custom CsvFileHandler, define it as specified in the configuration.
    """

    app_config: configuration.AppConfig = None

    def __init__(self, landing_directory, archive_directory):
        # Call the superclass __init__ method
        super().__init__(landing_directory, archive_directory)
        self.app_config = instance_pool.get_object("app_cfg")

    def load_file(self, file_path, workflow_trace_id, batch_id, domain):
        """
        Load data from a CSV file.

        This method should be implemented in subclasses to load data from the CSV file
        and perform any necessary processing.

        Parameters:
            file_path (str): The path to the CSV file to be loaded.
            workflow_trace_id (str): The workflow Trace ID
            batch_id (str): The batch_id for file batch_process
            domain: payment
        """
        logger.info(
            "DefaultCsvFileHandler -> Start processing using load Default CSV file handler : {0}, batch_id: {1}".format(
                file_path, batch_id))
        table = self.app_config.domains[domain].table
        csv_table_mapping = self.app_config.domains[domain].csv_table_mapping

        # Read the CSV table mapping file
        mapping: dict
        try:
            with open(csv_table_mapping, mode='r', newline='', encoding='utf-8') as mapping_file:
                mapping_reader = csv.reader(mapping_file)
                mapping = {rows[0].lower(): rows[1].lower() for rows in mapping_reader}
            logger.info(f"CSV table mapping: {mapping}")
        except FileNotFoundError as e:
            logger.error("CSV table mapping file not found: {}".format(e))
            return False
        except Exception as e:
            logger.error("Error reading CSV table mapping file: {}".format(e))
            return False
        # Get the table schema
        table_schema = get_table_schema(table)
        logger.info(f"Table schema: {table_schema}")
        # Get the id_field from model_data_features
        id_field = get_table_id_field(table)
        if not id_field:
            logger.error(f"Could not determine id field for table {table}")
            return False
        # Columns to skip
        skip_columns = ['id', 'batch_id', id_field]
        # Determine missing columns
        missing_columns = {col: col_type for col, col_type in table_schema.items() if
                           col not in mapping.values() and col not in skip_columns}
        logger.info(f"Missing columns: {missing_columns}")
        datas = []
        try:
            logger.info(f"Opening CSV file: {file_path}")
            with open(file_path, mode='r', newline='', encoding='utf-8') as file:
                csv_reader = csv.DictReader(file)
                for row_number, row in enumerate(csv_reader, start=1):
                    # Log the contents of the current row for debugging
                    # logger.info(f"Row {row_number}: {row}")
                    row = {
                        k.lower(): v for k, v in row.items()
                    }
                    try:
                        # Create a dictionary based on the mapping
                        item_data = {mapping[key]: convert_value(table_schema, mapping[key], row[key]) for key in
                                     mapping if
                                     key in row}
                        # Add missing columns with default values
                        for col, col_type in missing_columns.items():
                            item_data[col] = get_default_value(col_type)
                        # Log the created item_data dictionary
                        # logger.debug(f"Log the created item_data dictionary mapping: {row_number}: {item_data}")
                        # Add batch_id and a random item_id directly
                        item_data['batch_id'] = batch_id

                        # Log the created item_data dictionary
                        # logger.info(f"item_data {row_number}: {item_data}")

                        # Ensure only necessary columns are included in the final data
                        required_columns = ['batch_id', id_field] + list(table_schema.keys())
                        filtered_item_data = {k: item_data[k] for k in required_columns if k in item_data}

                        datas.append(filtered_item_data)
                    except KeyError as e:
                        logger.error(f"Missing key in row {row_number}: {e}")
                        continue

            logger.info(f"Total records loaded: {len(datas)}")

            if datas:
                # Print each key-value pair in the first element of datas for debugging
                # logger.info(f"One line of datas: {datas[0]}")
                # logger.info(f"Keys in datas[0]: {list(datas[0].keys())}")
                save_item_datas(datas, workflow_trace_id, table)
            else:
                logger.info("No data found in the CSV file.")
            # If processing is successful, move the file to the archive folder
            logger.info(
                f"Completed load CSV file: workflow_trace_id: {workflow_trace_id}, \
                file_path: {file_path}, batch_id: {batch_id}")
            return True
        except Exception as e:
            # If an error occurs during processing, handle the error
            logger.error(f"Error loading CSV file: {e}")
            return False


def save_item_datas(datas, workflow_trace_id, table):
    if not datas:
        logger.error("No data to save")
        return 0

    # Get the column names from the keys of the first dictionary in datas
    columns = datas[0].keys()

    # Create the SQL INSERT statement dynamically
    sql_insert_query = f"""
       INSERT INTO {table} (
           {', '.join(columns)}
       ) VALUES (
           {', '.join(['%s' for _ in columns])}
       )
    """
    logger.info(f"sql_insert_query: {sql_insert_query}")

    # Prepare a list of tuples from the data attributes
    values = [tuple(d[col] for col in columns) for d in datas]

    # Log the first value for debugging purposes
    logger.info(f"values[0]: {values[0] if values else 'No values to insert'}")

    try:
        total_records = execute_batch_insert(sql_insert_query, values)
        logger.info(
            f"workflow_id: {workflow_trace_id} - Completed load CSV file to {table}, total records: {total_records}")
        return total_records
    except Exception as e:
        logger.error(f"Error inserting data: {e}")
        return 0


def get_default_value(col_type):
    if col_type in ['integer', 'int']:
        return 0
    elif col_type in ['decimal', 'numeric', 'real', 'double precision']:
        return convert_to_decimal_db_value('0.00')
    elif col_type in ['boolean']:
        return False
    elif 'date' in col_type or 'timestamp' in col_type:
        return datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    else:
        return ''


def convert_value(table_schema, key, value):
    col_type = table_schema.get(key)
    if col_type:
        if col_type in ['integer', 'int']:
            return int(value)
        elif col_type in ['decimal', 'numeric', 'real', 'double precision']:
            return convert_to_decimal_db_value(value, '0.00')
        elif col_type in ['boolean']:
            return value.lower() in ['true', '1']
        elif 'date' in col_type or 'timestamp' in col_type:
            return value if value else datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    return value


def convert_to_decimal_db_value(input_value, precision='0.00'):
    try:
        # Convert the input to Decimal
        result = Decimal(input_value)
        # Quantize the Decimal to the specified precision
        result = result.quantize(Decimal(precision))
        # logger.info(f"convert_to_decimal: {input_value} -> {result}")
        return str(result)
    except InvalidOperation:
        # Handle the case where input_value is not a valid number
        logger.error(f"Invalid input for decimal conversion: {input_value}")
        return str(Decimal(precision))
