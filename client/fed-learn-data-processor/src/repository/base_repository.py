from src.repository.db.db_connection import DBConnection
import logging as logger


def execute_batch_insert(sql_insert_query, values):
    with DBConnection() as db_connection:
        return db_connection.execute_batch_insert(sql_insert_query, values)


def get_table_schema(table_name):
    """
    Get the table schema from the database.

    Parameters:
        table_name (str): The name of the table.

    Returns:
        dict: A dictionary with column names as keys and their data types as values.
    """
    schema = {}
    try:
        sql = f"""SELECT lower(column_name), data_type FROM information_schema.columns WHERE table_name = '{table_name}'"""
        with DBConnection() as db_connection:
            result = db_connection.execute_query(sql)
            for row in result:
                schema[row[0]] = row[1]

    except Exception as e:
        logger.error(f"Error fetching table schema: {e}")
    return schema
