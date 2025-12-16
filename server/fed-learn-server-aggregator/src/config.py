import os
from os.path import dirname as opd, realpath as opr
from dotenv import load_dotenv

_BASEDIR_ = opd(opr(__file__))
load_dotenv(dotenv_path=_BASEDIR_ + "/app.env")


# Define a function to access configuration values
def get_config(key):
    return os.environ.get(key)
