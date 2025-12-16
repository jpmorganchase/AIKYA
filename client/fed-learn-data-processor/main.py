import sys

from os.path import dirname as opd, realpath as opr

from src.repository.db.db_connection import test_db_connection
from src import instance_pool
import uvicorn
from fastapi import FastAPI
from contextlib import asynccontextmanager
from fastapi.middleware.cors import CORSMiddleware

from src.router.data_load_router import data_load_router
from src.router.data_info_router import data_info_router
from src.service.processor.data_metadata_init_executor import SeedMetadataExecutor, IP_SEED_META_EXEC_INST_KEY
from src.watcher.file_watcher_handler import FileWatcherHandler, IP_FILE_WATCHER_HANDLER_INST_KEY

import logging
from logging import config
from src import configuration

_BASEDIR_ = opd(opr(__file__))
sys.path.append(_BASEDIR_)


@asynccontextmanager
async def app_lifecycle(fast_api_app: FastAPI):
    # load the app config from the instance pool. this is inserted when the configuration is loaded
    # in the configuration module
    app_config: configuration.AppConfig = instance_pool.get_object(configuration.IP_APP_CONFIG_INST_KEY)

    # initialize the database
    logging.info("Initializing DB Connection...")
    if not test_db_connection():
        error_message = "Client DB connection failed! \
            Data processor service depends on the client db running. \
            Please check the DB service or please confirm the data processor application configuration"
        logging.error(error_message)
        raise RuntimeError(error_message)

    logging.info(f"Database connected: ok")

    logging.info("Initializing file watcher, seed executor...")

    file_watcher_handler = FileWatcherHandler(app_config.file_watcher, app_config.file)
    instance_pool.load_object(IP_FILE_WATCHER_HANDLER_INST_KEY, file_watcher_handler)

    file_watcher_handler.start_watcher()
    instance_pool.load_object(IP_SEED_META_EXEC_INST_KEY, SeedMetadataExecutor(app_config.file))

    instance_pool.get_object(IP_SEED_META_EXEC_INST_KEY).execute()

    try:
        yield
    finally:
        file_watcher_handler.stop_watcher()


def create_app(app_config: configuration.AppConfig):
    app: FastAPI = FastAPI(lifespan=app_lifecycle)

    # Set up CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],  # Allows all origins
        allow_credentials=True,
        allow_methods=["*"],  # Allows all methods
        allow_headers=["*"],  # Allows all headers
    )

    app.include_router(data_load_router)
    app.include_router(data_info_router)
    return app


# Legacy mode
if __name__ == "__main__":
    configuration.load_config()
    cl_args = instance_pool.get_object(configuration.IP_CL_ARGS_INST_KEY)
    log_cfg: dict = instance_pool.get_object(configuration.IP_LOG_CFG_INST_KEY)
    app_cfg: configuration.AppConfig = instance_pool.get_object(configuration.IP_APP_CONFIG_INST_KEY)

    logging.config.dictConfig(log_cfg["dict_config"])
    logging.info("Logger initialized. Creating app...")
    app: FastAPI = create_app(app_cfg)

    logging.info("App initialized. Starting app at port: {}".format(app_cfg.port))

    uvicorn.run(app, host=app_cfg.host, port=int(app_cfg.port))
