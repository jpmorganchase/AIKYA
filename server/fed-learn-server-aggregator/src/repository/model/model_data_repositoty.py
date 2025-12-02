import traceback
from datetime import datetime

from mysql.connector import Error

from src.repository.db.db_connection import DBConnection
from src.util import log

logger = log.init_logger()


def get_model_info(domain):
    """
    Returns an empty tuple if no record is found.
    """
    sql = (
        """SELECT id, model_name, model_definition FROM model_definition WHERE domain='{}'"""
        .format(domain))
    result = DBConnection.execute_query(sql)
    if result:
        return result[0][0], result[0][1], result[0][2]
    return ()



def save_model_aggregate_result(workflow_trace_id, client_id, model_id, group_hash, num_examples, metrics_aggregated,
                                parameters):
    try:
        logger.info("----  Entered saving model aggregate result  ------")
        logger.info("Before getting database connection")
        connection = DBConnection.get_connection()
        logger.info("Database connection obtained")
        if connection.is_connected():
            logger.info("Database connection is active")
            cursor = connection.cursor()
            logger.info("Database cursor created")
            logger.info(f"metrics_aggregated: {metrics_aggregated}")
            # Insert metrics
            if isinstance(metrics_aggregated, dict) and metrics_aggregated:
                logger.info("Inserting metrics into the database")
                for name, value in metrics_aggregated.items():
                    value = round(value, 6)  # Round the value to 6 decimal places
                    insert_metrics_query = """
                        INSERT INTO metrics (workflow_trace_id, source, name, value)
                        VALUES (%s, %s, %s, %s)
                    """
                    cursor.execute(insert_metrics_query, (workflow_trace_id, 'SERVER', name, value))
                    logger.info(f"Saved metric - {name}: {value}")
            else:
                logger.warning("No metrics to insert or metrics_aggregated is not a dictionary.")


            logger.info("Fetching the latest model version from the database")
            find_latest_version_query = "SELECT MAX(version) FROM model_aggregate_weights WHERE model_id = %s"
            cursor.execute(find_latest_version_query, (model_id,))
            result = cursor.fetchone()
            latest_version = result[0] if result and result[0] else None
            logger.info(f"Current latest version for model: {model_id} -> V-{latest_version}")
            new_version = (latest_version + 1) if latest_version is not None else 1
            logger.info(f"New version for model: {model_id} -> V-{new_version}")


            # Insert model aggregate weights result with new version
            insert_aggregate_weights_query = """
                INSERT INTO model_aggregate_weights (workflow_trace_id, model_id, version, parameters)
                VALUES (%s, %s, %s, %s)
            """
            cursor.execute(insert_aggregate_weights_query, (workflow_trace_id, model_id, new_version, parameters))
            model_weights_id = cursor.lastrowid
            logger.info(f"saved aggregate weights (model_aggregate_weights) with NEW VERSION: {new_version}")

            # Insert run model aggregation
            insert_run_model_aggregation_query = """
                INSERT INTO run_model_aggregation
                (workflow_trace_id, client_id, model_id, group_hash, model_weights_id, loss, num_examples, status)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """
            loss = round(metrics_aggregated.get("loss", 0.0), 6)
            cursor.execute(insert_run_model_aggregation_query, (
                workflow_trace_id, client_id, model_id, group_hash, model_weights_id, loss, num_examples,
                'Complete'))
            logger.info(f"save run model aggregation -> {workflow_trace_id}, model: {model_id}, client: {client_id}")
            # Find model_collaboration_run by group_hash
            find_collaboration_run_query = "SELECT id, current_round, rounds FROM model_collaboration_run WHERE group_hash = %s"
            cursor.execute(find_collaboration_run_query, (group_hash,))
            collaboration_run = cursor.fetchone()

            if collaboration_run:
                collaboration_run_id, current_round, rounds = collaboration_run
                new_round = current_round + 1

                # Update current_round and set started_at if initial round
                update_collaboration_run_query = """
                             UPDATE model_collaboration_run
                             SET current_round = %s,
                                 started_at = IF(current_round = 0, %s, started_at),
                                 status = IF(%s = rounds, 'Complete', status),
                                 completed_at = IF(%s = rounds, %s, completed_at)
                             WHERE id = %s
                         """
                cursor.execute(update_collaboration_run_query, (
                    new_round, datetime.now(), new_round, new_round, datetime.now(), collaboration_run_id))
                logger.info("update model collaboration run")
            connection.commit()
            logger.info("Model aggregate result saved successfully")
        else:
            logger.error("Database connection is not active")
    except Exception as e:
            logger.error(f"Error saving model aggregate result: {e}")
            logger.error(traceback.format_exc())
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()
            logger.info("MySQL cursor and connection are closed")
