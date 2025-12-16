import logging as logger

from src.repository.db.db_connection import DBConnection


def get_data_process_workflow_id(workflow_trace_id):
    """
    Get the workflow ID and domain type for a given file name.

    Parameters:
        workflow_trace_id (str): worklfow trace ID.

    Returns:
        tuple: A tuple containing the workflow ID and domain type corresponding to the file name,
               or an empty tuple if not found.
    """
    sql = "SELECT workflow_trace_id, batch_id, domain_type FROM data_seed WHERE workflow_trace_id='{}' AND status='Initial'".format(
        workflow_trace_id)
    with DBConnection() as db_connection:
        result = db_connection.execute_query(sql)
        if result:
            return result[0][0], result[0][1], result[0][2]
    return ()  # Return an empty tuple if no workflow ID found


def update_data_process_status(workflow_trace_id, status):
    """
    Update the status of a data process in the database.

    Parameters:
        workflow_trace_id (str): The ID of the workflow.
        status (str): The new status to set.
    """

    sql = "UPDATE data_seed SET status='{}' WHERE workflow_trace_id='{}'".format(status, workflow_trace_id)
    logger.info(f"SQL Query: {sql}")
    with DBConnection() as db_connection:
        db_connection.execute_update(sql)


def describe_table(table_name):
    """
     Describe the structure of a table in the database.

     Parameters:
         table_name (str): The name of the table to describe.

     Returns:
         list: A list containing the names of the columns in the specified table.
     """

    sql = "DESCRIBE {}".format(table_name)
    logger.info("SQL: {}".format(sql))
    # Create a cursor object to execute SQL queries
    with DBConnection() as db_connection:
        table_structure = db_connection.execute_query(sql)
        # Extract column names from the structure information
        column_names = [column_info[0].upper() for column_info in table_structure]

        # Return the list of column names
        return column_names


def is_data_process_exist(workflow_trace_id):
    """
    Check if a data process exists in the database for a given workflow ID.

    Parameters:
        workflow_trace_id (str): The ID of the workflow to check.

    Returns:
        bool: True if the data process exists, False otherwise.
    """
    sql = "SELECT COUNT(*) FROM data_seed WHERE workflow_trace_id='{}'".format(workflow_trace_id)
    with DBConnection() as db_connection:
        result = db_connection.execute_query(sql)
        if result[0][0] == 0:
            return False

    return True


def get_data_process(workflow_trace_id):
    """
    Check if a data process exists in the database for a given workflow ID.

    Parameters:
        workflow_trace_id (str): The ID of the workflow to check.

    Returns:
        bool: True if the data process exists, False otherwise.
    """
    sql = "SELECT workflow_trace_id, status FROM data_seed WHERE workflow_trace_id='{}' ".format(workflow_trace_id)
    with DBConnection() as db_connection:
        result = db_connection.execute_query(sql)
        if result:
            return result[0][0], result[0][1]

    return ()  # Return an empty tuple if no workflow ID found


def get_table_data_count(workflow_trace_id):
    """
    Get the count of records in domain_payment_data table filtered by batch_id.

    Args:
        workflow_trace_id (str): The workflow_trace_id to filter the records.
    Returns:
        int: The count of records matching the workflow_trace_id.
    """
    try:
        with DBConnection() as db_connection:
            data_seed_query = """SELECT domain_type, batch_id FROM data_seed WHERE workflow_trace_id='{0}'""".format(
                workflow_trace_id)
            domain_type, batch_id = db_connection.execute_fetch_one(data_seed_query)
            db_table_query = """SELECT db_table FROM model_data_features WHERE domain='{0}' LIMIT 1""".format(
                domain_type)
            result = db_connection.execute_fetch_one(db_table_query)
            if not result:
                logger.info(f"No db_table found for domain_type: {domain_type}")
                return 'No data', workflow_trace_id, "", 0
            db_table = result[0]
            data_count_query = """SELECT COUNT(*) FROM {0} WHERE batch_id ='{1}'""".format(db_table, batch_id)
            result = db_connection.execute_fetch_one(data_count_query)
            if not result:
                logger.info(f"No records found in table {db_table} for batch_id: {batch_id}")
                return 'No data', workflow_trace_id, db_table, 0
            record_count = result[0]
            return 'Success', workflow_trace_id, db_table, record_count
    except Exception as e:
        logger.error(f"Error getting record count: {e}")
        return 'Error', workflow_trace_id, "", 0


def create_data_process(workflow_trace_id, batch_id, file_path, file_name, label, model, domain_type, is_mock_data):
    """
    create data process status to the database.

    Parameters:
        workflow_trace_id (str): The ID of the workflow.
        batch_id (str): The ID of the batch process.
        file_path (str): The path of the file.
        file_name (str): The name of the file.
        label (str): The label.
        model (str): The model.
        domain_type (str): The domain type.
        is_mock_data (str): Whether the data is mock or real.
    """
    sql = """INSERT INTO data_seed(workflow_trace_id, batch_id, file_path, file_name, label, model, domain_type, is_mock_data, status)
             VALUES('{}', '{}', '{}', '{}', '{}', '{}', '{}', '{}', '{}')""".format(
        workflow_trace_id, batch_id, file_path, file_name, label, model, domain_type, is_mock_data, 'Initial')
    with DBConnection() as db_connection:
        db_connection.execute_query(sql)
