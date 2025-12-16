from src.repository.db.db_connection import DBConnection
from src.repository.process.seeds_repository import get_id_field
import logging as logger


class DataPredictExecutor:
    def __init__(self, workflow_trace_id, batch_id, domain):
        self.workflow_trace_id = workflow_trace_id
        self.batch_id = batch_id
        self.domain = domain

    def execute(self):
        logger.info("{0} - DataPredictExecutor process predict init data".format(self.workflow_trace_id))
        self.handle_predict_init_data()

    def handle_predict_init_data(self):
        logger.info("Executing Predict Init Data for payment")
        if not self.has_predict_init_data():
            # Get the initial data
            logger.info("Get Predict Init Data for payment batchId: {0}".format(self.batch_id))
            init_data = self.get_predict_init_data()
            logger.info(
                "found Predict Init Data for batchId: {0}, size: {1}".format(self.batch_id, len(init_data)))
            # Prepare the batch insert
            values = []
            for data in init_data:
                values.append((
                    self.workflow_trace_id,
                    self.domain,
                    self.batch_id,
                    data['itemId'],
                    ''  # result is empty for now
                ))

            sql_insert_query = (
                "INSERT INTO model_predict_data "
                "(workflow_trace_id, domain, batch_id, item_id, result) "
                "VALUES (%s, %s, %s, %s, %s)"
            )

            # Execute the batch insert
            logger.info("Executing batch insert for Predict Init Data")
            with DBConnection() as db_connection:
                total_records = db_connection.execute_batch_insert(sql_insert_query, values)
            logger.info("Total records inserted: {0}".format(total_records))
        else:
            logger.info("payment Predict Init Data has already been processed ")

    def has_predict_init_data(self):
        sql = "SELECT item_id FROM model_predict_data WHERE batch_id='{}'".format(self.batch_id)
        logger.info("Predict Init Data query: {0}".format(sql))
        with DBConnection() as db_connection:
            item_ids = db_connection.execute_query(sql)
        return len(item_ids) > 0

    def get_predict_init_data(self):
        # Retrieve the id_field dynamically from the database
        id_field = get_id_field(self.domain)
        if not id_field:
            return []
        sql = "select {} as item_id from domain_{}_data where batch_id='{}'".format(id_field, self.domain,
                                                                                    self.batch_id)
        logger.info("{0} Predict query: {1}".format(self.domain, sql))
        with DBConnection() as db_connection:
            item_ids = db_connection.execute_query(sql)
        result = [{'batchId': self.batch_id, 'itemId': item[0]} for item in item_ids]
        logger.info("{0} Predict size: {1} - {2}".format(self.domain, len(result), self.batch_id))
        return result
