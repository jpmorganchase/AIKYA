from src.repository.process.seeds_repository import create_data_seed_metadata, has_data_seed_metadata
import logging as logger
from src.configuration import FileConfig

IP_SEED_META_EXEC_INST_KEY = "seed_executor"


class SeedMetadataExecutor:
    def __init__(self, file_config: FileConfig):
        self.node = file_config.node

    def execute(self):
        try:
            # Convert self.node to an integer
            node_num = int(self.node)
            domain = "payment"
            logger.info("load seed metadata info, node: {0}".format(node_num))
            count = has_data_seed_metadata(domain)
            if count == 0:
                logger.info("create data seed metadata, node: {0}".format(node_num))
                create_data_seed_metadata(node_num, domain)
            else:
                logger.info("data seed metadata info exists, node: {0}".format(node_num))

        except ValueError:
            logger.error(f"Invalid node number: {self.node}")
            raise
