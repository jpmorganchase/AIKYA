from src.repository.db.db_connection import DBConnection
import logging


def update_global_model_track(name, global_model_weights, global_weights_version):
    """
    Update the status of a data process in the database.

    Parameters:
        name (str): The name of model.
        global_model_weights (str): The global model weights
        global_weights_version (str): The global model weights version
    """

    sql = """UPDATE agent_model_records SET global_model_weights='{}' and global_weights_version='{}' WHERE name='{}'""".format(
        global_model_weights, global_weights_version, name)
    logging.info(f"SQL Query: {sql}")
    with DBConnection() as db_connection:
        db_connection.execute_update(sql)


def update_local_model_track(name, local_model_weights, local_weights_version):
    """
    Update the status of a data process in the database.

    Parameters:
        name (str): The name of model.
        local_model_weights (str): The local model weights
        local_weights_version (str): The local model weights version
    """

    sql = """UPDATE agent_model_records SET local_model_weights='{}' and local_weights_version='{}' WHERE name='{}'""".format(
        local_model_weights, local_weights_version, name)
    logging.info(f"SQL Query: {sql}")
    with DBConnection() as db_connection:
        db_connection.execute_update(sql)


def create_model_track(name, definition, model_version, domain_type):
    """
    create data process status to the database.

    Parameters:
        name (str): The name of model.
        definition (str): The model definition.
        model_version (str): The version of the model.
        domain_type (str): domain type such as payment.
    """
    sql = """insert into agent_model_records (name, definition, model_version, domain) VALUES('{}', '{}', '{}', '{}')""".format(
        name, definition, model_version, domain_type)
    logging.info(f"SQL Query: {sql}")
    with DBConnection() as db_connection:
        db_connection.execute_update(sql)


def create_workflow_model_process(workflow_trace_id, event, status):
    sql = """INSERT INTO workflow_model_logs (workflow_trace_id, event, status)
               VALUES ('{}', '{}', '{}')""".format(workflow_trace_id, event, status)
    logging.info(f"SQL Query: {sql}")
    with DBConnection() as db_connection:
        db_connection.execute_update(sql)
