import logging
import sys
from contextlib import asynccontextmanager
from os.path import dirname as opd, realpath as opr

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from src.router.aggregate_router import agg_router
from src.router.aggregator_simulation_router import agg_simulation_router
from src.util import log

logger = log.init_logger()

_BASEDIR_ = opd(opr(__file__))
sys.path.append(_BASEDIR_)
from dotenv import load_dotenv

load_dotenv(dotenv_path=_BASEDIR_ + "/app.env")


@asynccontextmanager
async def app_lifecycle(fast_api_app: FastAPI):
    try:
        logging.info("Aikya FL Server started and is listening on http://0.0.0.0:9001")
    except Exception as e:
        logging.error("Unexpected error: %s", e)
    yield
    # add any shutdown logic here
    pass


def create_app():
    logging.getLogger("uvicorn.error").setLevel(logging.INFO)
    app: FastAPI = FastAPI(lifespan=app_lifecycle)

    # Set up CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],  # Allows all origins
        allow_credentials=True,
        allow_methods=["*"],  # Allows all methods
        allow_headers=["*"],  # Allows all headers
    )
    app.include_router(agg_router, prefix="/aggregate/api")
    app.include_router(agg_simulation_router, prefix="/aggregate/api")
    return app


# Legacy mode
if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app = create_app()
    uvicorn.run(app, host="0.0.0.0", port=9001)
