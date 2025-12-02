import csv
import logging as logger
import os
import shutil
from datetime import datetime
from src.repository.process.model_track_repository import create_workflow_model_process
from src.service.processor.data_predict_executor import DataPredictExecutor
from src.repository.process.process_repository import update_data_process_status, describe_table
from src import instance_pool
from src.configuration import AppConfig, IP_APP_CONFIG_INST_KEY

""" Abstract base class for csv file processing """


class CsvFileHandler:
    def __init__(self, landing_directory, archive_directory):
        self.app_cfg: AppConfig = instance_pool.get_object(
            IP_APP_CONFIG_INST_KEY)
        """
        Initialize the CsvFileHandlerTemplate.

        Parameters:
            landing_directory (str): The directory where CSV files are initially placed.
            archive_directory (str): The directory where processed CSV files are archived.
        """
        self.landing_directory = landing_directory
        self.archive_directory = archive_directory

    def process_csv_file(self, file_path, workflow_trace_id, batch_id, domain):
        """
        Process a CSV file.

        This method executes the processing steps for a CSV file, including validation,
        loading, workflow update, and moving to the archive directory.

        Parameters:
            file_path (str): The path to the CSV file to be processed.
            workflow_trace_id (str): The workflow ID
            batch_id (str): The batch_id for file batch_process
        """
        try:
            logger.info("found Workflow Trace ID: {}".format(
                workflow_trace_id))

            is_valid, csv_table_mapping = self.validate(file_path, domain)
            if is_valid:
                is_load_file_success = self.load_file(
                    file_path, workflow_trace_id, batch_id, domain)
                logger.info(
                    "WorkflowTraceID {0} - is_load_file_success: {1}".format(workflow_trace_id, is_load_file_success))
                if is_load_file_success:
                    update_predict_init_data(
                        workflow_trace_id, batch_id, domain)
                    update_data_process_status(workflow_trace_id, 'Complete')
                    logger.info(
                        "load file Complete for CSV file: : {0}".format(file_path))
                else:
                    update_data_process_status(workflow_trace_id, 'Fail')
                    update_predict_init_data(
                        workflow_trace_id, batch_id, domain)
                    logger.error(
                        "load file Fail for CSV file: : {0}".format(file_path))
            else:
                update_data_process_status(workflow_trace_id, 'Fail')
                logger.error(
                    "Validation error for loading CSV file: : {0}".format(file_path))

            self.move_to_archive(file_path)
        except Exception as e:
            # If an error occurs during processing, handle the error
            # always move to archive or error
            update_data_process_status(workflow_trace_id, 'Fail')
            self.move_to_archive(file_path)
            logger.error("Error processing CSV file: : {0}".format(e))

    def load_file(self, file_path, workflow_trace_id, batch_id, domain):
        """
        Load data from a CSV file.

        This method should be implemented in subclasses to load data from the CSV file
        and perform any necessary processing.

        Parameters:
            file_path (str): The path to the CSV file to be loaded.
            workflow_trace_id (str): The workflow ID
            batch_id (str): The batch_id for file batch_process
            domain: file domain
        """
        pass

    def validate(self, file_path, domain_type):
        """
        Validate a CSV file.

        This method should be implemented in subclasses to perform validation
        specific to the CSV file format or content.

        Parameters:
            :param file_path: The path to the CSV file to be validated.
            :param domain_type: domain of file to be validated.
        """
        # Set a flag to track whether all columns are mapped
        logger.info("Start validation: {0}".format(file_path))
        csv_table_mapping = self.load_csv_table_mapping(domain_type)
        is_all_db_column_mapped = self.validate_table_header(
            csv_table_mapping, domain_type)
        logger.info("is_all_db_column_mapped: {0}".format(
            is_all_db_column_mapped))
        is_all_csv_column_mapped = self.validate_csv_header(
            file_path, csv_table_mapping)
        logger.info("is_all_csv_column_mapped: {0}".format(
            is_all_csv_column_mapped))

        all_mapped = is_all_db_column_mapped and is_all_csv_column_mapped
        logger.info("Completed validation: {0}".format(all_mapped))
        # Return the flag value
        return all_mapped, csv_table_mapping

    def move_to_archive(self, file_path):
        # Create archive folder if it doesn't exist
        if not os.path.exists(self.archive_directory):
            os.makedirs(self.archive_directory)

        # Get current date
        current_date_time = datetime.now().strftime("%Y%m%d_%H%M%S")

        # Generate new file name with current date appended
        file_name, file_extension = os.path.splitext(
            os.path.basename(file_path))
        new_file_name = f"{file_name}_{current_date_time}{file_extension}"

        # Move the CSV file to the archive folder with the new name
        archive_path = os.path.join(self.archive_directory, new_file_name)
        shutil.move(file_path, archive_path)

        return archive_path

    def load_csv_table_mapping(self, domain_type):
        """
        Load CSV file containing mapping between CSV headers and database table columns.

        Parameters:
            domain_type (str): The domain of the CSV file.

        Returns:
            dict: A dictionary mapping CSV headers to database table columns.
        """
        csv_mapping = {}

        try:
            csv_table_mapping_name = self.app_cfg.domains[domain_type].csv_table_mapping
            logger.info("load csv_table_mapping: {}".format(
                csv_table_mapping_name))
            # Define the path to the CSV mapping file
            project_root = os.path.abspath(os.path.join(
                os.path.dirname(__file__), "../../.."))
            mapping_file_path = os.path.join(
                project_root, csv_table_mapping_name)
            logger.info("Resolved mapping file path: {}".format(
                mapping_file_path))
            with open(mapping_file_path, 'r') as file:
                csv_reader = csv.DictReader(file)
                for row in csv_reader:
                    csv_mapping[row['CSV_HEADER']] = row['DB_TABLE_COLUMN']

        except Exception as e:
            logger.error("Error processing CSV Table mapping: {0}".format(e))
        return csv_mapping

    def validate_csv_header(self, csv_file_path, csv_table_mapping):
        """
        Validate if the CSV file header matches the database table columns.

        Parameters:
            csv_file_path (str): The path to the CSV file.
            csv_table_mapping (dict): A dictionary mapping CSV headers to database table columns.

        Returns:
            bool: True if CSV header matches the database table columns, False otherwise.
        """
        try:
            logger.info(
                "validate_csv_header load CSV file: : {0}".format(csv_file_path))
            logger.info(f"csv_table_mapping: {csv_table_mapping}")
            with open(csv_file_path, 'r') as file:
                csv_reader = csv.reader(file)
                csv_header = next(csv_reader)  # Get the header row
                # Log all CSV headers
                logger.info(f"CSV headers: {csv_header}")
                # Check if each CSV header corresponds to a database table column
                for header in csv_header:
                    if header not in csv_table_mapping.keys():
                        logger.error(
                            f"CSV header '{header}' does not match any database table column in the mapping.")
                        return False  # If any header doesn't match, return False

        except FileNotFoundError as e:
            logger.error("Error CSV file: {0}".format(e))

        return True  # If all headers match, return True

    def validate_table_header(self, csv_mapping, domain_type):
        """
        Validate if all database columns defined in csv_mapping are present in the database table.

        Parameters:
            csv_mapping (dict): A dictionary mapping CSV headers to database table columns.
            domain_type (str): The domain type, e.g., "payment".

        Returns:
            bool: True if all database columns are present in the database table, False otherwise.
        """
        # Get the database table name
        table_name = self.app_cfg.domains[domain_type].table
        logger.info("Table name: {0}".format(table_name))

        # Get the database table columns
        table_columns = describe_table(table_name)
        logger.info(f"Database table columns: {table_columns}")

        # Check if each database column in csv_mapping is present in table_columns
        for db_column in csv_mapping.values():
            if db_column.upper() not in table_columns:
                logger.error(
                    "CSV DB Column {0} is not found in the table columns".format(db_column))
                return False  # Return False if any database column is not present

        return True  # Return True if all database columns are present


def update_predict_init_data(workflow_trace_id, batch_id, domain):
    executor = DataPredictExecutor(workflow_trace_id, batch_id, domain)
    executor.execute()
