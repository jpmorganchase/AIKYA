##########################################################################
############# START: FED-LEARN-CLIENT-AGENT IMAGE DEFINITION #############
##########################################################################

FROM python:3.12-slim AS fl-client-agent-stg

SHELL [ "/bin/bash", "-c" ]

ARG CLIENT_NODE_NUMBER

WORKDIR /apps

# ensure system is up to date and install basic utilities
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y curl tree \
    && apt-get autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*;

# copy contents
COPY . .

# copy scaling dataset files
COPY --from=datasets \
    ./payment_fraud/bank${CLIENT_NODE_NUMBER}*scaling*.csv \
    ./data/scaling/payment_fraud/node${CLIENT_NODE_NUMBER}/

# copy training dataset files
COPY --from=datasets \
    ./payment_fraud/bank${CLIENT_NODE_NUMBER}*gen_train*.csv \
    ./data/genesis/payment_fraud/node${CLIENT_NODE_NUMBER}/

# install python packages
RUN tree ./data; \
    python -m pip install -U --no-cache-dir pip setuptools wheel;\
    python -m pip install -U --no-cache-dir -r requirements.txt;

##########################################################################
############## END: FED-LEARN-CLIENT-AGENT IMAGE DEFINITION ##############
##########################################################################

