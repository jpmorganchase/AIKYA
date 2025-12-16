##########################################################################
############# START: FED-LEARN-CLIENT-AGENT IMAGE DEFINITION #############
##########################################################################

FROM python:3.12-alpine AS fl-server-aggregate-stg

# SHELL [ "/bin/bash", "-c" ]

ARG root_dir=/apps
WORKDIR $root_dir

# copy contents
COPY . .

# create the app directory structure recursively and install system packages
RUN mkdir -p "$root_dir"/logs \
    && apk update && apk add --no-cache python3-dev \
    && python3 -m pip install --no-compile --no-cache-dir -U -r requirements.txt

CMD ["python", "main.py"]

##########################################################################
############ END: FED-LEARN-SERVER-AGGREGATE IMAGE DEFINITION ############
##########################################################################

