""" logging utility for project """

import sys
import logging.config
from os.path import dirname, abspath, join
_BASEDIR_ = dirname(dirname(dirname(abspath(__file__))))
sys.path.append(_BASEDIR_)

def init_logger():
    """ Initialize logger with configuration from log.conf """
    logging.config.fileConfig(join(_BASEDIR_, "log.conf"))
    # Determine the log level or target based on an environment variable or default to 'prod'
    logger_type = sys.argv[1] if len(sys.argv) > 1 else "prod"
    logger = logging.getLogger("dev" if logger_type == "DEV" else "prod")
    return logger
