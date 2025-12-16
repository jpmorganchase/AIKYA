##########################################################################
##########################################################################
################ START: MYSQL IMAGE DEFINITION ###########################
##########################################################################

FROM mysql:9.5 AS fl-client-db-stg

WORKDIR /docker-entrypoint-initdb.d/

# Build argument to specify the node directory
ARG NODE_DIR

# Conditionally copy the SQL files from the NODE_DIR if provided, otherwise from the default directory
COPY sql/*.sql .
COPY ${NODE_DIR:-default}/*.sql .

CMD ["mysqld", "--max-allowed-packet=256M"]

##########################################################################
################### END: MYSQL IMAGE DEFINITION ##########################
##########################################################################
##########################################################################
