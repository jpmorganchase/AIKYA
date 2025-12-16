import csv

from src.service.file.csv_file_handler import CsvFileHandler
import logging as logger


# from src.util import log
# logger = log.init_logger()


class SampleCsvFileHandler(CsvFileHandler):
    """ Concrete implementation of CsvFileHandlerTemplate """

    def __init__(self, landing_directory, archive_directory):
        # Call the superclass __init__ method
        super().__init__(landing_directory, archive_directory)

    def load_file(self, file_path, workflow_id, batch_id, domain):
        """
        Load data from a CSV file.

        This method should be implemented in subclasses to load data from the CSV file
        and perform any necessary processing.

        Parameters:
            file_path (str): The path to the CSV file to be loaded.
        """
        logger.info("Start processing load CSV file: : {0}".format(file_path))
        try:
            with open(file_path, 'r') as file:
                csv_reader = csv.reader(file)
                for row in csv_reader:
                    # Process each row
                    logger.info(row)  # Example: Print each row
                    # save to database

            # If processing is successful, move the file to the archive folder
            logger.info("Completed load CSV file: : {0}".format(file_path))
            return True
        except Exception as e:
            # If an error occurs during processing, handle the error
            logger.error("Error load CSV file: : {0}".format(e))
            return False
