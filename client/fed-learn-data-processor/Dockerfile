##########################################################################
############ START: FED-LEARN-DATA-PROCESSOR IMAGE DEFINITION ############
##########################################################################

FROM python:3.12-slim AS fl-data-processor-stg

SHELL [ "/bin/bash", "-c" ]

ARG CLIENT_NODE_NUMBER

ARG root_dir=/apps

WORKDIR $root_dir

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y curl tree \
    && apt-get autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*; \
    mkdir -p "$root_dir"/data/archive \
    "$root_dir"/data/landing \
    "$root_dir"/data/seeds \
    "$root_dir"/data/external

# copy contents
COPY . .

COPY --from=datasets ./**/bank${CLIENT_NODE_NUMBER}*.csv ./data/seeds/

# create the app directory structure recursively and install system packages
RUN tree ./data; \
    python3 -m pip install --upgrade --no-cache-dir pip setuptools wheel; \
    python3 -m pip install --upgrade --no-cache-dir -r requirements.txt;

##########################################################################
############# END: FED-LEARN-DATA-PROCESSOR IMAGE DEFINITION #############
##########################################################################
