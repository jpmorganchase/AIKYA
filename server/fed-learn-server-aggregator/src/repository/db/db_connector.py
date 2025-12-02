import mysql.connector
from src.config import get_config
from src.util import log

logger = log.init_logger()

class DBConnector:
    def __new__(cls):
        if not hasattr(cls, 'instance'):
            cls.instance = super(DBConnector, cls).__new__(cls)
        return cls.instance

    def __init__(self):
        self.host = get_config("app.datasource.host")
        self.port = get_config("app.datasource.port")
        self.database = get_config("app.datasource.database")
        self.user = get_config("app.datasource.user")
        self.password = get_config("app.datasource.password")
        self.dbconn = None

    # creates new connection
    def create_connection(self):
        config = {
            'user': self.user,
            'password': self.password,
            'host': self.host,
            'port': self.port,
            'database': self.database,
            'raise_on_warnings': True
        }
        try:
            return mysql.connector.connect(**config)
        except mysql.connector.Error as err:
            logger.error(f"Error connecting to the database: {err}")
            return None

    # For explicitly opening database connection
    def __enter__(self):
        self.dbconn = self.create_connection()
        return self.dbconn

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.dbconn.close()
