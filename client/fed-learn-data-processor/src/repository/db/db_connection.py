import mysql.connector
import logging
from src import configuration, instance_pool


# IP_DB_CONN_INST_KEY = "db_connection"


def test_db_connection() -> bool:
    try:
        with DBConnection() as db_connection:
            return db_connection.is_connected()
    except Exception as e:
        logging.error(e)

    return False


def _connect():
    app_config: configuration.AppConfig = instance_pool.get_object("app_cfg")
    connect_config = {
        "user": app_config.data_source.user,
        "password": app_config.data_source.password,
        "host": app_config.data_source.db_host,
        "port": app_config.data_source.db_port,
        "database": app_config.data_source.database,
    }
    return mysql.connector.connect(**connect_config)


class DBConnection(object):
    _connection = None

    def __init__(self):
        self._connection = _connect()

    def __del__(self):
        if self._connection and self.is_connected():
            self._connection.close()

    def __enter__(self):
        if not self._connection or not self.is_connected():
            self._connection = _connect()

        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self._connection is not None and self.is_connected():
            self._connection.commit()
            self._connection.close()

    def is_connected(self):
        return self._connection and self._connection.is_connected()

    def execute_query(self, query):
        """execute query on singleton db connection"""
        try:
            with self._connection.cursor() as cursor:
                cursor.execute(query)
                return cursor.fetchall()
        except mysql.connector.Error as err:
            logging.error(err)

        return None

    def execute_fetch_one(self, query):
        """execute query on singleton db connection"""

        try:
            with self._connection.cursor() as cursor:
                cursor.execute(query)
                return cursor.fetchone()
        except mysql.connector.Error as err:
            logging.error(err)

        return None

    def execute_update(self, query):
        """execute query on singleton db connection"""

        try:
            with self._connection.cursor() as cursor:
                cursor.execute(query)
                self._connection.commit()
                return cursor.rowcount

        except mysql.connector.Error as err:
            logging.error(err)

        return None

    # Function to execute a batch insert

    def execute_batch_insert(self, query, values):
        try:
            with self._connection.cursor() as cursor:
                cursor.executemany(query, values)
                self._connection.commit()
                return cursor.rowcount
        except mysql.connector.Error as err:
            logging.error(err)
        return None
