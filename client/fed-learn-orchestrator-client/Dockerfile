##########################################################################
######### START: FED-LEARN-ORCHESTRATOR-CLIENT IMAGE DEFINITION ##########
##########################################################################

FROM maven:3.9-eclipse-temurin-22-alpine AS fl-client-orchestrator-builder-stg

WORKDIR /apps

COPY --from=aikya_commons fed-learn-orchestrator-core/ /apps/fed-learn-orchestrator-core/
COPY ./fed-learn-orchestrator-client /apps/fed-learn-orchestrator-client/

WORKDIR /apps/fed-learn-orchestrator-core
RUN mvn clean package install

WORKDIR /apps/fed-learn-orchestrator-client
RUN mvn clean package install

WORKDIR /apps/bin

FROM eclipse-temurin:22-jdk AS fl-client-orchestrator-stg

RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /apps/quorum/keys

COPY --from=fl-client-orchestrator-builder-stg /apps/fed-learn-orchestrator-core/target/*.jar   /apps/
COPY --from=fl-client-orchestrator-builder-stg /apps/fed-learn-orchestrator-client/target/*.jar /apps/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "/apps/fed-learn-orchestrator-client-0.0.1-SNAPSHOT.jar"]

##########################################################################
########## END: FED-LEARN-ORCHESTRATOR-CLIENT IMAGE DEFINITION ###########
##########################################################################