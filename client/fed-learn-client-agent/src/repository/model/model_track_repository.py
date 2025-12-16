import mysql
import json

from src.repository.db.db_connection import DBConnection
from src.util import log

logger = log.init_logger()


def update_global_model_track(name, global_model_weights, global_weights_version):
    """
    Update the status of a data process in the database.

    Parameters:
        name (str): The name of model.
        global_model_weights (str): The global model weights
        global_weights_version (str): The global model weights version
    """

    sql = """UPDATE model_client_records SET global_model_weights='{}' and global_weights_version='{}' WHERE name='{}'""".format(
        global_model_weights, global_weights_version, name)
    print(f"SQL Query: {sql}")
    DBConnection.execute_update(sql)


def update_local_model_track(name, local_model_weights, local_weights_version):
    """
    Update the status of a data process in the database.

    Parameters:
        name (str): The name of model.
        local_model_weights (str): The local model weights
        local_weights_version (str): The local model weights version
    """

    sql = """UPDATE model_client_records SET local_model_weights='{}' and local_weights_version='{}' WHERE name='{}'""".format(
        local_model_weights, local_weights_version, name)
    print(f"SQL Query: {sql}")
    DBConnection.execute_update(sql)


def create_model_track_records(
        name, definition, model_version, domain_type, local_model_weights,
        local_weights_version):
    """
    create data process status to the database.

    Parameters:
        name (str): The name of model.
        definition (str): The model definition.
        model_version (str): The version of the model.
        domain_type (str): domain type such as payment.
        local_model_weights (str): weights.
        local_weights_version (int): version of weights.
    """
    sql = """insert into model_client_records (name, definition, model_version, domain, local_model_weights, local_weights_version) VALUES('{}', '{}', '{}', '{}', '{}', '{}')""".format(
        name, definition, model_version, domain_type, local_model_weights, local_weights_version)
    DBConnection.execute_update(sql)


def get_model_track_record(domain):
    """
    Retrieve the global model track for the given domain from the database.

    Parameters:
        domain (str): The domain for which to retrieve the global model track.

    Returns:
        tuple: A tuple containing the model definition, model version, local model weights,
                local weights version, global model weights, and global weights version.
                Returns an empty tuple if no record is found.
    """
    sql = (
        """select definition, model_version, local_model_weights, local_weights_version, global_model_weights, global_weights_version from model_client_records where name='{}'"""
        .format(domain))
    result = DBConnection.execute_query(sql)
    if result:
        return result[0][0], result[0][1], result[0][2], result[0][3], result[0][4], result[0][5]
    return ()


def create_local_model_historical_records(workflow_trace_id, name, model_weights):
    """
    create data process status to the database.

    Parameters:
        name (str): The name of model.
        workflow_trace_id (str): workflow trace id.
        model_weights (str): local model weights.
    """
    max_version_sql = """SELECT IFNULL(MAX(version), 0) FROM model_client_record_history WHERE name ='{}'""".format(
        name)
    result = DBConnection.execute_fetch_one(max_version_sql)
    max_version = result[0] + 1
    logger.info(
        "create_local_model_historical_records max_version: {0}".format(max_version))
    sql = """
    INSERT INTO model_client_record_history (workflow_trace_id, name, model_weights, version) VALUES ('{}', '{}', '{}','{}')""".format(
        workflow_trace_id, name, model_weights, max_version)
    DBConnection.execute_update(sql)
    logger.info(
        "created local model historical records, name: {0}, workflow_trace_id: {1}, max_version: {2}".format(
            name,
            workflow_trace_id,
            max_version)
    )


def upsert_model_process(workflow_trace_id, event, status):
    # Check if the record exists with the given workflow_trace_id and event
    check_sql = """SELECT COUNT(*) FROM workflow_model_logs WHERE workflow_trace_id = '{}' AND event = '{}'""".format(
        workflow_trace_id, event)
    result = DBConnection.execute_query(check_sql)

    if result[0][0] > 0:
        # If the record exists, update the status
        update_workflow_model_process(workflow_trace_id, event, status)
        print(
            f"Updated model process with workflow_trace_id: {workflow_trace_id} and event: {event}")
    else:
        # If the record does not exist, insert a new one
        create_workflow_model_process(workflow_trace_id, event, status)
        print(
            f"Inserted new model process with workflow_trace_id: {workflow_trace_id} and event: {event}")


def create_workflow_model_process(workflow_trace_id, event, status):
    sql = """INSERT INTO workflow_model_logs (workflow_trace_id, event, status)
             VALUES ('{}', '{}', '{}')""".format(workflow_trace_id, event, status)
    DBConnection.execute_update(sql)


def update_workflow_model_process(workflow_trace_id, event, status):
    sql = """UPDATE workflow_model_logs
             SET status='{}'
             WHERE workflow_trace_id='{}' AND event='{}'""".format(status, workflow_trace_id, event)
    DBConnection.execute_update(sql)


def update_local_weight_definition(name, definition, model_weights):
    sql = ""
    if definition and model_weights:
        sql = """update model_client_records values set definition = '{}' and local_model_weights = '{}' WHERE name = '{}'""".format(
            definition, model_weights, name)
    elif definition:
        sql = """update model_client_records set definition = '{}' WHERE name = '{}'""".format(
            definition, name)
    DBConnection.execute_update(sql)


def create_and_update_model_weight_records(workflow_trace_id, name, model_weights, domain):
    # Get the current global max version for the given domain
    sql_global_version = """SELECT COALESCE(MAX(global_weights_version), 0)
                            FROM model_client_records
                            WHERE domain='{}'""".format(domain)
    logger.info(f"Executing: {sql_global_version}")
    global_version_result = DBConnection.execute_query(sql_global_version)
    global_max_version = global_version_result[0][0] if global_version_result else 1
    # Get the current max version for the given name
    sql_max_version = """SELECT COALESCE(MAX(version), 0) + 1
                         FROM model_client_record_history
                         WHERE name='{}'""".format(name)
    logger.info(f"Executing: {sql_max_version}")
    max_version_result = DBConnection.execute_query(sql_max_version)
    local_history_max_version = max_version_result[0][0] if max_version_result else 1
    # The version to use should be the maximum of the local and global versions
    new_version = max(global_max_version + 1, local_history_max_version)
    # Insert the new record with the new version
    sql_insert = """INSERT INTO model_client_record_history (workflow_trace_id, name, model_weights, version)
                    VALUES ('{}', '{}', '{}', {})""".format(workflow_trace_id, name, model_weights, new_version)
    logger.info(f"Executing: {sql_insert}")
    DBConnection.execute_update(sql_insert)
    # Update the model_client_records with the new version and reset local_model_weights
    sql_update = """UPDATE model_client_records
                       SET local_weights_version={}, local_model_weights='{}'
                       WHERE name='{}'""".format(new_version, model_weights, name)
    logger.info(f"Executing: {sql_update}")
    DBConnection.execute_update(sql_update)


def save_mode_training_result(workflow_trace_id, num_examples, metrics):
    """
    Save the model training results into the database.

    Args:
        workflow_trace_id (str): The workflow trace identifier.
        num_examples (int): The number of examples used in the evaluation.
        metrics (dict): A dictionary containing the metrics from the model evaluation (e.g., accuracy).

    Returns:
        None
    """
    try:
        connection = DBConnection.get_connection()
        if connection.is_connected():
            cursor = connection.cursor()
            # Insert metrics
            # Insert metrics
            for name, value in metrics.items():
                value = round(value, 6)  # Round the value to 10 decimal places
                insert_metrics_query = """
                                INSERT INTO metrics (workflow_trace_id, source, name, value)
                                VALUES (%s, %s, %s, %s)
                            """
                insert_values = (workflow_trace_id, 'CLIENT', name, value)
                logger.info(f"Executing query: {insert_metrics_query}")
                cursor.execute(insert_metrics_query, insert_values)
                logger.info(f"Saved metric - {name}: {value}")

                # Insert model training result
            insert_training_result_query = """
                  INSERT INTO model_training_result (workflow_trace_id, num_examples)
                  VALUES (%s, %s)
              """
            cursor.execute(insert_training_result_query,
                           (workflow_trace_id, num_examples))
            connection.commit()
            logger.info("Model training result saved successfully")
    except mysql.connector.Error as e:
        logger.error(f"Error saving model training result: {e}")
    finally:
        if connection.is_connected():
            cursor.close()
            logger.info("MySQL cursor is closed")


def insert_shapley_values(workflow_trace_id: str, domain: str, batch_id: str, values_json: list[dict]):
    query = "insert into model_predict_shap_data (workflow_trace_id, domain, batch_id, shapley_values) values (%s, %s, %s, %s)"
    values = [
        (workflow_trace_id, domain, batch_id, json.dumps(value_json))
        for value_json in values_json
    ]
    DBConnection.execute_batch_insert(query, values)
    # try:
    #     connection = DBConnection.get_connection()
    #     if connection.is_connected():
    #         cursor = connection.cursor()
    #         # insert query
    #         query = """
    #         insert into model_predict_shap_data (workflow_trace_id, domain, batch_id, shapley_values)
    #         values (%s, %s, %s, %s)
    #         """
    #         cursor.execute(query, (workflow_trace_id, domain, batch_id, json.dumps(values_json)))
    #         connection.commit()
    # except Exception as ex:
    #     logger.exception(ex, exc_info=True, stack_info=True)
