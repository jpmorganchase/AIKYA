import logging as logger
import os
import shutil

from src import instance_pool
from src.configuration import AppConfig, IP_APP_CONFIG_INST_KEY
from src.model.data_load_req import DataLoadRequest
from src.repository.process.process_repository import is_data_process_exist, create_data_process
from src.repository.process.seeds_repository import get_data_seed_label


class DataProcessExecutor:

    def __init__(self, data_load_req: DataLoadRequest):
        self.app_config: AppConfig = instance_pool.get_object(IP_APP_CONFIG_INST_KEY)
        self.data_load_req = data_load_req
        self.delimiter = self.app_config.file.delimiter

    def execute(self, action):

        is_mock_enabled = self.data_load_req.mock_enabled
        if is_mock_enabled == "T":
            logger.info("run data mock")
        else:
            if action == "seed":
                self.execute_file_process()
            else:
                logger.info("action {0} is not supported!".format(action))

    """
       Abstract base class for file processing executors.
    """

    def execute_file_process(self):
        self.initial_track_process()

    def get_data_seed_file_path(self):
        seeds_folder = self.app_config.file_watcher.seeds_dir
        seed_file = os.path.join(seeds_folder, self.data_load_req.file_name)
        return str(seed_file)

    def initial_track_process(self):
        seed_file = self.get_data_seed_file_path()
        landing_dir = self.app_config.file_watcher.landing_dir
        workflow_trace_id = self.data_load_req.workflow_trace_id
        is_process_started = is_data_process_exist(workflow_trace_id)
        if not is_process_started:
            file_name = self.data_load_req.file_name
            model = self.data_load_req.model
            domain_type = self.data_load_req.domain_type
            mock_enabled = self.data_load_req.mock_enabled
            batch_id = self.data_load_req.batch_id
            label = get_data_seed_label(domain_type, file_name)

            logger.info(
                "create data process Workflow: {0} Batch: {1} file: {2}".format(workflow_trace_id, batch_id, file_name))
            create_data_process(workflow_trace_id, batch_id, landing_dir, file_name, label, model, domain_type,
                                mock_enabled)
            copy_file(workflow_trace_id, file_name, seed_file, landing_dir, self.delimiter)
        return seed_file


def copy_file(workflow_trace_id, file_name, source_file, destination_folder, delimiter):
    """
    Copy a file from source folder to destination folder.

    Parameters:
        workflow_trace_id (str): trace workflow process.
        file_name: copy file name
        source_file (str): The path to the source file.
        destination_folder (str): The path to the destination folder.
        delimiter: split file with delimiter

    Returns:
        str: The path to the copied file in the destination folder.
    """
    try:
        # Copy the file to the destination folder
        new_file_name = f"{workflow_trace_id}{delimiter}{file_name}"
        destination_file_path = os.path.join(destination_folder, new_file_name)
        logger.info(
            "Workflow: {0} - Start to copy file from seeds {1}, dest: {2}".format(workflow_trace_id, source_file,
                                                                                  destination_file_path))
        shutil.copy(source_file, destination_file_path)
        logger.info("Workflow: {0} - Completed copying file to {1}".format(workflow_trace_id, destination_file_path))
        return destination_file_path
    except Exception as e:
        logger.error(
            "Workflow: {0}, Error copying file: source: {1}, target: {2}. {3}".format(workflow_trace_id, source_file,
                                                                                      destination_folder, e))
        return None
