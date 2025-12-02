import os
import time

from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer

from src.repository.process.process_repository import get_data_process_workflow_id
from src.service.file.file_hander_helper import get_conf_file_handler
import logging as logger
from src import configuration
import threading

IP_FILE_WATCHER_HANDLER_INST_KEY = "watch_handler"


class FileWatcherHandler(FileSystemEventHandler):
    landing_directory: str
    archive_directory: str
    delimiter: str

    def __init__(
        self,
        watcher_config: configuration.FileWatcherConfig,
        file_config: configuration.FileConfig,
    ):
        super().__init__()
        self.landing_directory = watcher_config.landing_dir
        self.archive_directory = watcher_config.archive_dir
        self.delimiter = file_config.delimiter
        self.observer = Observer()
        self.lock = threading.Lock()  # Ensure thread safety

    def start_watcher(self):
        self.observer.schedule(self, path=self.landing_directory)
        self.observer.start()
        logger.info("File watcher started.")

    def stop_watcher(self):
        self.observer.stop()
        self.observer.join()  # Wait for the observer to finish
        logger.info("File watcher stopped.")

    def on_created(self, event):
        """
        Handles the event when a new file or directory is created in the watched path.
        This method is triggered by the file system watcher whenever a creation event occurs.
        It can be extended to process or log newly created files or directories as needed.
        Args:
            event: The event object containing information about the created file or directory.
        """
        pass

    def process_file(self, file_path):
        try:
            # Example: Simulate reading and processing a CSV file
            file_name = os.path.basename(file_path)
            logger.info(f"process_file, file_name {file_name}")
            parts = file_name.split(self.delimiter, 1)
            if len(parts) > 1:
                workflow_trace_id = parts[0]
                workflow_trace_id, batch_id, domain_type = get_data_process_workflow_id(
                    workflow_trace_id
                )
                logger.info(
                    "file watcher service found Workflow Trace ID: {0}, BatchId: {1}".format(
                        workflow_trace_id, batch_id
                    )
                )
                file_handler = get_file_handler(
                    domain_type, self.landing_directory, self.archive_directory
                )
                file_handler.process_csv_file(
                    file_path, workflow_trace_id, batch_id, domain_type
                )
            else:
                # Workflow ID is empty
                logger.warning(
                    f"Workflow ID not found for file, skip process {file_name}"
                )
        except Exception as e:
            logger.error(f"Error loading CSV file {file_path}: {e}")
            raise

    def on_modified(self, event):
        with self.lock:
            logger.info(f"File {event.src_path} has been modified")
            file_path = event.src_path
            if self.is_copy_finished(file_path):
                logger.info(f"File copy finished, processing file: {file_path}")
                self.process_file(file_path)
            else:
                logger.info(f"File copy not finished yet: {file_path}")

    def on_deleted(self, event):
        """
        Handles the event when a file or directory is deleted in the watched path.
        This method is triggered by the file system watcher whenever a deletion event occurs.
        It can be extended to process or log deleted files or directories as needed.
        Args:
            event: The event object containing information about the deleted file or directory.
        """
        pass

    def on_moved(self, event):
        if event.src_path.endswith(".tmp"):
            logger.info("Ignoring temporary file: {0}".format(event.src_path))
            return
        logger.info(f"File {event.src_path} has been moved to {event.dest_path}")

    def is_copy_finished(self, file_path):
        try:
            initial_size = -1
            while True:
                current_size = os.path.getsize(file_path)
                if current_size == initial_size:
                    return True
                initial_size = current_size
                time.sleep(1)
        except OSError as e:
            logger.error(f"Error checking if copy is finished: {e}")
        return False


def get_file_handler(domain_type, landing_dir, archive_dir):
    file_handler = get_conf_file_handler(domain_type, landing_dir, archive_dir)
    return file_handler
