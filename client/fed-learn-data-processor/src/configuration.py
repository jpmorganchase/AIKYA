import argparse
import json
import os.path
from dataclasses import dataclass

from src import instance_pool

IP_APP_CONFIG_INST_KEY = "app_cfg"
IP_CL_ARGS_INST_KEY = "cl_args"
IP_LOG_CFG_INST_KEY = "log_cfg"


class CustomEncoder(json.JSONEncoder):
    def default(self, o):
        return o.__dict__


class ConfigBase:
    def __str__(self):
        return json.dumps(self.__dict__, cls=CustomEncoder, indent=4)


@dataclass
class FileWatcherConfig(ConfigBase):
    enabled: bool
    seeds_dir: str
    landing_dir: str
    archive_dir: str

    def override(self, override_from):
        self.enabled = (
            override_from.enabled
            if hasattr(override_from, "enabled") and override_from.enabled is not None
            else self.enabled
        )
        self.seeds_dir = (
            override_from.seeds_dir
            if hasattr(override_from, "seeds_dir")
            and override_from.seeds_dir is not None
            else self.seeds_dir
        )
        self.landing_dir = (
            override_from.landing_dir
            if hasattr(override_from, "landing_dir")
            and override_from.landing_dir is not None
            else self.landing_dir
        )
        self.archive_dir = (
            override_from.archive_dir
            if hasattr(override_from, "archive_dir")
            and override_from.archive_dir is not None
            else self.archive_dir
        )


@dataclass
class DataSourceConfig(ConfigBase):
    db_host: str
    db_port: str
    database: str
    user: str
    password: str

    def override(self, override_from):
        self.db_host = (
            override_from.db_host
            if hasattr(override_from, "db_host") and override_from.db_host is not None
            else self.db_host
        )
        self.db_port = (
            override_from.db_port
            if hasattr(override_from, "db_port") and override_from.db_port is not None
            else self.db_port
        )
        self.database = (
            override_from.database
            if hasattr(override_from, "database") and override_from.database is not None
            else self.database
        )
        self.user = (
            override_from.db_user
            if hasattr(override_from, "db_user") and override_from.db_user is not None
            else self.user
        )
        self.password = (
            override_from.db_password
            if hasattr(override_from, "db_password") and override_from.db_password is not None
            else self.password
        )


@dataclass
class FileConfig(ConfigBase):
    node: int
    delimiter: str

    def override(self, override_from):
        self.node = (
            override_from.node
            if hasattr(override_from, "node") and override_from.node is not None
            else self.node
        )
        self.delimiter = (
            override_from.delimiter
            if hasattr(override_from, "delimiter")
            and override_from.delimiter is not None
            else self.delimiter
        )


@dataclass
class DomainItemConfig(ConfigBase):
    table: str
    csv_table_mapping: str


@dataclass
class AppConfig(ConfigBase):
    host: str
    port: str
    file_watcher: FileWatcherConfig
    data_source: DataSourceConfig
    domains: dict[str, DomainItemConfig]
    file: FileConfig

    def override(self, override_from):
        self.port = (
            override_from.port
            if hasattr(override_from, "port") and override_from.port is not None
            else self.port
        )

    def __init__(self, host, port, file_watcher, data_source, domains, file):
        self.file_watcher = (
            FileWatcherConfig(**file_watcher) if file_watcher is not None else None
        )
        self.data_source = (
            DataSourceConfig(**data_source) if data_source is not None else None
        )
        self.file = FileConfig(**file) if file is not None else None
        self.domains = (
            {domain: DomainItemConfig(**domains[domain]) for domain in domains}
            if domains is not None
            else []
        )
        self.port = port if port is not None else "7001"
        self.host = host if host is not None else "localhost"


def _override_app_config(cl_args, config: AppConfig):
    if config.data_source is not None:
        config.data_source.override(cl_args)
    if config.file_watcher is not None:
        config.file_watcher.override(cl_args)
    return config


def _override_logger_config(cl_args, config: dict):
    if "log_dir" in config:
        config["log_dir"] = (
            cl_args.log_dir
            if (hasattr(cl_args, "log_dir") and cl_args.log_dir is not None)
            else config["log_dir"]
        )
        if (
            "dict_config" in config
            and "handlers" in config["dict_config"]
            and "file" in config["dict_config"]["handlers"]
        ):
            config["dict_config"]["handlers"]["file"]["filename"] = os.path.join(
                config["log_dir"], "fl-data-learn.log"
            )

    return config


def _load_cla():
    """loads command line args. please don't use this function directly.
    instead use load_config()

    Returns:
        Namespace: an object containing command line parameters
    """
    cl_parser = argparse.ArgumentParser(description="Federated learning data processor")
    cl_parser.add_argument(
        "-f",
        "--config-file",
        type=str,
        required=True,
        help="Application config file in json format",
    )
    cl_parser.add_argument(
        "-dh",
        "--db-host",
        type=str,
        required=False,
        default="localhost",
        help="Database host name. this will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-dp",
        "--db-port",
        type=str,
        required=False,
        default="3306",
        help="Database port number. This will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-u",
        "--db-user",
        type=str,
        required=False,
        default="aikya",
        help="Database user name. This will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-pwd",
        "--db-password",
        type=str,
        required=False,
        default="",
        help="Database password. This will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-sd",
        "--seeds-dir",
        type=str,
        required=False,
        default=None,
        help="Seeds directory. This will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-ld",
        "--landing-dir",
        type=str,
        required=False,
        default=None,
        help="Landing directory. This will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-ad",
        "--archive-dir",
        type=str,
        required=False,
        default=None,
        help="Archive directory. This will override the value provided in the config file",
    )
    cl_parser.add_argument(
        "-l",
        "--log-dir",
        type=str,
        required=False,
        default=None,
        help=(
            "Log directory for file stream. This will override the value provided in the config \
                                 file"
        ),
    )
    return cl_parser.parse_args()


def load_config() -> None:
    """
    Loads application, logger configuration and command line arguments.
    To access these, use instance_pool.get_object() using keys
        IP_CL_ARGS_INST_KEY
        IP_APP_CONFIG_INST_KEY
        IP_LOG_CFG_INST_KEY
    """
    cl_args = _load_cla()
    with open(cl_args.config_file, "r") as f:
        json_config = json.load(f)
        app_config = (
            _override_app_config(cl_args, AppConfig(**json_config["app"]))
            if "app" in json_config
            else None
        )
        logger_config = (
            _override_logger_config(cl_args, json_config["logging"])
            if "logging" in json_config
            else {}
        )

    instance_pool.load_object(IP_CL_ARGS_INST_KEY, cl_args)
    instance_pool.load_object(IP_LOG_CFG_INST_KEY, logger_config)
    instance_pool.load_object(IP_APP_CONFIG_INST_KEY, app_config)


if __name__ == "__main__":
    load_config()
    _end: str = "\n------------\n"
    print(
        f"Command Line Args\n{instance_pool.get_object(IP_CL_ARGS_INST_KEY)}",
        end=_end,
    )
    print(
        f"App Configuration\n{instance_pool.get_object(IP_APP_CONFIG_INST_KEY)}",
        end=_end,
    )
    print(
        f"Logging Dict Config\n{instance_pool.get_object(IP_LOG_CFG_INST_KEY)}",
        end=_end,
    )
