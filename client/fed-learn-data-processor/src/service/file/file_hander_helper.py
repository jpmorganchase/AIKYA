import importlib.util
import os

from src.service.file.default_csv_file_handler import DefaultCsvFileHandler

from src import instance_pool, configuration

import logging


def get_domain_file_conf(domain_type):
    app_config: configuration.AppConfig = instance_pool.get_object(configuration.IP_APP_CONFIG_INST_KEY)
    # Read the configuration
    table = app_config.domains[domain_type].table
    csv_table_mapping = app_config.domains[domain_type].csv_table_mapping
    return table, csv_table_mapping, None, None


def get_conf_file_handler(domain_type, landing_dir, archive_dir):
    # Fetch the domain configuration
    table, csv_table_mapping, file_handler_path, file_handler_class_name = get_domain_file_conf(domain_type)
    if file_handler_path and file_handler_class_name:
        # Convert the module path to an absolute path relative to the project root
        # Assuming the root of the project is two levels up from the current file
        project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..'))
        module_path = os.path.join(project_root, file_handler_path.replace('/', os.sep))

        # Extract module name from the file path
        module_name = os.path.splitext(os.path.basename(module_path))[0]

        # Debugging: print paths to verify correctness
        logging.info(f"Module name: {module_name}")
        logging.info(f"Module path: {module_path}")

        # Check if the file exists
        if not os.path.isfile(module_path):
            raise FileNotFoundError(f"No such file: '{module_path}'")

        # Dynamically import the module
        spec = importlib.util.spec_from_file_location(module_name, module_path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        if hasattr(module, file_handler_class_name):
            handler_class = getattr(module, file_handler_class_name)
            return handler_class(landing_dir, archive_dir)
        else:
            raise AttributeError(f"The module {module_name} does not have a class named '{file_handler_class_name}'.")

    # If specific handler is not defined, use the default handler
    logging.info("Using default handler")
    return DefaultCsvFileHandler(landing_dir, archive_dir)
