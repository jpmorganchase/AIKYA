import logging as logger

from src.repository.db.db_connection import DBConnection


def get_data_seed_metadata(domain_type):
    """
    Get the data seed metadata  for a given domain type.

    Parameters:
        domain_type (str): The name of the data seed  domain type.

    Returns:
        tuple: A tuple containing the file name and labels corresponding to the related domain,
               or an empty tuple if not found.
    """
    sql = "select id, file_name, label from data_seed_metadata where domain_type='{}'".format(
        domain_type)

    with DBConnection() as db_connection:
        return db_connection.execute_query(sql)


def get_data_seed_label(domain_type, file_name):
    sql = "select label from data_seed_metadata where domain_type='{}' and file_name='{}'".format(
        domain_type, file_name)

    with DBConnection() as db_connection:
        result = db_connection.execute_fetch_one(sql)
        return result[0] if result else None  # Assuming 'label' is the third column


def get_data_seed_domain(workflow_trace_id):
    """
    Get the data seed metadata  for a given domain type.

    Parameters:
        workflow_trace_id (str): workflow trace id

    Returns:
        tuple: A tuple containing the file name and labels corresponding to the related domain,
               or an empty tuple if not found.
    """
    sql = "select batch_id, domain_type from data_seed where workflow_trace_id='{}'".format(workflow_trace_id)

    with DBConnection() as db_connection:
        return db_connection.execute_query(sql)


def has_data_seed_metadata(domain_type):
    """
    Check if there is existing data in the data_seed_metadata table for a given domain_type.
    :param node_num: The node number to be included in the file name (not used in query)
    :param domain_type: The domain type to check for existing data
    :return: The count of records matching the domain_type
    """

    # SQL query to count records for the given domain_type
    sql = """SELECT COUNT(*) FROM data_seed_metadata WHERE domain_type ='{0}'""".format(domain_type)

    with DBConnection() as db_connection:
        # Execute the query
        return db_connection.execute_fetch_one(sql)[0]


def create_data_seed_metadata(node_num, domain_type):
    """
    Create the data seed metadata for a given node number.
    :param domain_type: seed domain
    :param node_num: The node number to be included in the file name
    """

    # Create SQL statements with dynamic node_num
    sql1 = "insert into data_seed_metadata (domain_type, file_name, label, is_mock_data) values ('{0}', 'data_init_bank{1}.csv', 'genesis data', false)".format(
        domain_type, node_num)
    sql2 = "insert into data_seed_metadata (domain_type, file_name, label, is_mock_data) values ('{0}', 'data_mix_bank{1}.csv', 'mix data', false)".format(
        domain_type, node_num)
    sql3 = "insert into data_seed_metadata (domain_type, file_name, label, is_mock_data) values ('{0}', 'data_fraud_bank{1}.csv', 'fraud data', false)".format(
        domain_type, node_num)
    sql4 = "insert into data_seed_metadata (domain_type, file_name, label, is_mock_data) values ('{0}', 'data_final_bank{1}.csv', 'final data', false)".format(
        domain_type, node_num)

    with DBConnection() as db_connection:
        db_connection.execute_update(sql1)
        db_connection.execute_update(sql2)
        db_connection.execute_update(sql3)
        db_connection.execute_update(sql4)
    return 0


def get_id_field(domain):
    # Retrieve the id_field dynamically from the database
    id_field_sql = "select id_field from model_data_features where domain='{}' limit 1".format(domain)
    with DBConnection() as db_connection:
        id_field_result = db_connection.execute_query(id_field_sql)
    id_field = id_field_result[0][0] if id_field_result else None

    if not id_field:
        logger.error("No id_field found for domain: {}".format(domain))

    return id_field


def get_table_id_field(table_name):
    """
    Get the distinct id_field from the model_data_features table for a given db_table.

    Parameters:
        table_name (str): The name of the db_table.

    Returns:
        str: The id_field name.
    """
    try:
        sql = "SELECT DISTINCT(id_field) FROM model_data_features WHERE db_table='{}'".format(table_name)

        with DBConnection() as db_connection:
            result = db_connection.execute_query(sql)
            return result[0][0] if result else None
    except Exception as e:
        logger.error(f"Error fetching id field for table {table_name}: {e}")
        return None
