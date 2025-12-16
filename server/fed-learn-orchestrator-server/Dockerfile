##########################################################################
######### START: FED-LEARN-ORCHESTRATOR-SERVER IMAGE DEFINITION ##########
##########################################################################

FROM maven:3.9-eclipse-temurin-22-alpine AS fl-server-orchestrator-builder-stg

WORKDIR /apps

COPY --from=aikya-commons fed-learn-orchestrator-core/ /apps/fed-learn-orchestrator-core/
COPY ./fed-learn-orchestrator-server/ /apps/fed-learn-orchestrator-server/

WORKDIR /apps/fed-learn-orchestrator-core
RUN mvn clean package install

WORKDIR /apps/fed-learn-orchestrator-server
RUN mvn clean package install

WORKDIR /apps/bin

FROM eclipse-temurin:22-jdk AS fl-server-orchestrator-stg

RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /apps/quorum/keys

COPY --from=fl-server-orchestrator-builder-stg /apps/fed-learn-orchestrator-core/target/*.jar   /apps/
COPY --from=fl-server-orchestrator-builder-stg /apps/fed-learn-orchestrator-server/target/*.jar /apps/

ENTRYPOINT ["java", "-jar", "/apps/fed-learn-orchestrator-server-0.0.1-SNAPSHOT.jar"]

##########################################################################
########## END: FED-LEARN-ORCHESTRATOR-SERVER IMAGE DEFINITION ###########
##########################################################################