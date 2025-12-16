from src.repository.db.db_connection import DBConnection
from src.util import log
import pandas as pd

logger = log.init_logger()


def get_model_feature_record(domain: str, batch_id: str) -> pd.DataFrame:
    """
    Constructs and executes a dynamic SQL query to retrieve records from the model_data_features table
    based on the specified domain and batch_id.

    Args:
        domain (str): The domain to filter the records.
        batch_id (str): The batch_id to filter the records.

    Returns:
        list: A list of tuples representing the records retrieved from the database.
    """
    # Construct the SQL query to retrieve db_table, id_field, and feature_field based on the domain
    sql = """
        SELECT db_table, id_field, feature_field
          FROM model_data_features 
         WHERE domain='{0}'
           AND model='{1}' AND status='Active'
         ORDER BY seq_num""".format(domain, domain)
    logger.info("get model_data_features sql: {0}".format(sql))
    # Execute the query and fetch the results
    rows = DBConnection.execute_query(sql)
    logger.info("get model_data_features rows: {0}".format(len(rows)))

    if not rows:
        logger.error("No active features found for the specified domain and model.")
        return pd.DataFrame()

    # Extract the table name and id field from the first row
    db_table = rows[0][0]
    id_field = rows[0][1]

    # Extract the feature fields from the remaining rows
    feature_fields = [row[2] for row in rows]

    # Construct the SELECT clause by joining the feature fields
    select_fields = [id_field] + feature_fields
    select_clause = ", ".join(select_fields)
    logger.info("start to model dynamic query")

    # Construct the dynamic SQL query to retrieve records based on the batch_id
    dynamic_query = f"SELECT {select_clause} FROM {db_table} WHERE batch_id='{batch_id}'"
    logger.info("model predict dynamic query: {0}".format(dynamic_query))

    # Execute the dynamic query and fetch the results
    result = DBConnection.execute_query(dynamic_query)
    df = pd.DataFrame(columns=select_fields, data=result)

    # Return the results
    logger.info("get dynamic query result: {0}".format(len(df)))
    return df


def get_model_training_record(domain: str, batch_id: str) -> pd.DataFrame:
    """
    Constructs and executes a dynamic SQL query to retrieve records from the model_data_features table
    based on the specified domain and batch_id, including joined results from model_predict_data and model_feedback.

    Args:
        domain (str): The domain to filter the records.
        batch_id (str): The batch_id to filter the records.

    Returns:
        list: A list of tuples representing the records retrieved from the database.
    """
    # Construct the SQL query to retrieve db_table, id_field, and feature_field based on the domain
    sql = """
        SELECT db_table, id_field, feature_field 
        FROM model_data_features 
        WHERE domain='{0}' AND model='{1}' AND status='Active'  
        ORDER BY seq_num
        """.format(domain, domain)
    logger.info("get model_data_features sql: {0}".format(sql))

    # Execute the query and fetch the results
    rows = DBConnection.execute_query(sql)
    logger.info("get model_data_features rows: {0}".format(len(rows)))

    if not rows:
        return pd.DataFrame()

    # Extract the table name and id field from the first row
    db_table = rows[0][0]
    id_field = rows[0][1]

    # Extract the feature fields from the remaining rows
    feature_fields = [row[2] for row in rows]

    # Construct the SELECT clause by joining the feature fields
    select_fields = [id_field] + feature_fields
    select_clause = ", ".join(select_fields)
    logger.info("start to model dynamic query")

    # Construct the dynamic SQL query to retrieve records based on the batch_id and join with model
    # predict_data and model_feedback
    dynamic_query = f"""
    SELECT {select_clause}, mdata.result, mdata.is_correct, mdata.score 
    FROM {db_table} data
    JOIN (
        SELECT mpd.item_id, mpd.result, mf.is_correct, mf.score 
        FROM model_predict_data mpd
        LEFT JOIN model_feedback mf ON mf.model_data_id = mpd.id
    ) mdata ON data.{id_field} = mdata.item_id
    WHERE data.batch_id = '{batch_id}'
    """
    logger.info("model training dynamic query: {0}".format(dynamic_query))
    # Execute the dynamic query and fetch the results
    result = DBConnection.execute_query(dynamic_query)
    dataframe_cols = select_fields + ["result", "is_correct", "score"]
    df = pd.DataFrame(columns=dataframe_cols, data=result)
    # Return the results
    logger.info("get dynamic query result: {0}".format(len(df)))
    return df
