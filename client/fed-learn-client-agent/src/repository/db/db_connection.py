import mysql.connector
from src.util import log
from src.repository.db.db_connector import DBConnector

logger = log.init_logger()


class DBConnection(object):
    connection = None

    @classmethod
    def get_connection(cls, new=False):
        """Creates return new Singleton database connection"""
        if new or not cls.connection or not cls.connection.is_connected():
            cls.connection = DBConnector().create_connection()
        return cls.connection

    @classmethod
    def execute_query(cls, query):
        """execute query on singleton db connection"""
        connection = cls.get_connection()
        try:
            cursor = connection.cursor()
        except mysql.connector.Error as err:
            logger.error(err)
            connection = cls.get_connection(new=True)  # Create new connection
            cursor = connection.cursor()
        cursor.execute(query)
        result = cursor.fetchall()
        cursor.close()
        return result

    @classmethod
    def execute_fetch_one(cls, query):
        """execute query on singleton db connection"""
        connection = cls.get_connection()
        try:
            cursor = connection.cursor()
        except mysql.connector.Error as err:
            logger.error(err)
            connection = cls.get_connection(new=True)  # Create new connection
            cursor = connection.cursor()
        cursor.execute(query)
        result = cursor.fetchone()
        cursor.close()
        return result

    @classmethod
    def execute_update(cls, query):
        """execute query on singleton db connection"""
        connection = cls.get_connection()
        try:
            cursor = connection.cursor()
        except mysql.connector.Error as err:
            logger.error(err)
            connection = cls.get_connection(new=True)  # Create new connection
            cursor = connection.cursor()
        cursor.execute(query)
        connection.commit()
        row_count = cursor.rowcount
        logger.info(f"{row_count} records inserted/updated successfully into table")
        cursor.close()
        return row_count

    # Function to execute a batch insert

    @classmethod
    def execute_batch_insert(cls, query, values):
        connection = cls.get_connection()
        try:
            cursor = connection.cursor()
        except mysql.connector.Error as err:
            logger.error(err)
            connection = cls.get_connection(new=True)  # Create new connection
            cursor = connection.cursor()
        # Prepare a list of tuples from the data attributes
        # Execute the batch insert
        cursor.executemany(query, values)
        connection.commit()
        row_count = cursor.rowcount
        logger.info(f"{row_count} records inserted successfully into table")
        cursor.close()
        return row_count

    @classmethod
    def execute_multiple_queries(cls, queries):
        try:
            connection = cls.get_connection(new=True)
            row_count = 0
            with connection.cursor() as cursor:
                bulk_query = "".join(queries)
                cursor.execute(bulk_query)
                while cursor.nextset():
                    result_set = cursor.fetchall()
                    row_count += 1

            connection.commit()
            connection.close()
            return row_count
        except mysql.connector.Error as err:
            logger.error(f"DB Error: {err}")
        return -1
