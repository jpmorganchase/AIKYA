# CSV to Database Data Loader

- [Overview](#overview)
- [Development](#development)
- [Deployment](#deployment)
  - [Overview](#overview-1)
  - [Run everything](#run-everything)
  - [Build images without running the containers](#build-images-without-running-the-containers)
- [Default File Handlers \& Configuration](#default-file-handlers--configuration)
  - [DefaultCsvFileHandler](#defaultcsvfilehandler)
  - [Example Usage](#example-usage)
- [Custom Handlers \& Configuration](#custom-handlers--configuration)
  - [Example Configuration](#example-configuration)
  - [Example PaymentCsvFileHandler](#example-paymentcsvfilehandler)

## Overview

This project provides a framework for converting CSV data into database rows using a mapping configuration. The default
handler, `DefaultCsvFileHandler`, automates the process by performing a direct 1-1 data conversion with proper type
conversion and adding missing columns by assigning default values. Custom handlers can also be created for specific
domains.

## Development

> These are notes pertinent if you are developing and contributing directly. ignore if you are using it.
> **Note that you need to run a mysql DB otherwise the application will crash**

To set up the project to run locally for debugging, testing, and verification

- Navigate in to the directory where `fed-learn-data-processor` is located.
- Create the following directory structure

    ```text
    fed-learn-data-processor
    |---- data
    |      |---- archive
    |      |---- landing
    |      |---- seeds
    |---- logs
    ```

    ```shell
    # on unix like environment
    mkdir -p data/archive data/landing data/seeds logs
    
    # on windows
    mkdir data/archive data/landing data/seeds logs 
    ```

- Once you have these directories created, proceed set up a python virtual environment and install all the dependencies

    ```shell
    # create a virtual env
    # replace with `python3` with `python` if your python interpreter is labelled as python instead of python3 
    python3 -m venv venv

    # activate the venv and install dependencies
    # for unix
    source venv/bin/activate

    # for windows
    venv/scripts/activate
  
    # install dependencies
    python3 -m pip install --no-cache-dir -r requirements.txt
    ```

- you can run the application as `python3 __main__.py -f config.json` now.

---

## Deployment

### Overview

The whole service is managed through `docker compose`. This includes

- building and configuring the images
- running the images
- setting up network config
- mounting volumes and files

the `compose.yml` defines a runtime and build definition for mysql and the data processor application.

- Although we are running a custom definition of the `mysql` image, it is the base image but with a few
  initialization files from the `sql` dir. Please refer to `/path/to/fed-learn-data-processor/docker/mysql/Dockerfile`
  for the definition.

### Run everything

There are two ways you can run containers

1. Build images locally and run containers based on those images.
2. Pull pre-built images from the server

To build the images, and then run the apps, you can simply run

```shell
docker compose build --no-cache --with-dependencies
docker compose up -d
```

### Build images without running the containers

If you wish to just build all the images without running and containers

```shell
docker compose build --no-cache --with-dependencies
```

To just build the mysql image

```shell
docker compose build --no-cache --with-dependencies fl-db
```

To just build the data processor service

```shell
docker compose build --no-cache --with-dependencies fl-data-processor
```



---

## Default File Handlers & Configuration

### DefaultCsvFileHandler

The DefaultCsvFileHandler class provides the default implementation for loading CSV data into the database. It performs
the following steps:

- Reads the CSV to database column mapping file.
- Loads the data from the CSV file.
- Converts the data to the appropriate types.
- Adds missing columns with default values.
- Inserts the data into the specified database table.

### Example Usage

By default, DefaultCsvFileHandler will be used if you specify the following configurations:

```env
#The database table to insert data into.
app.file.validate.{domain}.db.table: domain_payment_data
#The CSV to database column mapping file.
app.file.validate.{domain}.csv_table_mapping: conf/payment_csv_table_columns_mapping.csv
```

The user also needs to define the table and training feature information in the model_data_features table.

## Custom Handlers & Configuration

If you need custom conversion logic, you can create your own handler.

`PaymentCsvFileHandler` is provided as an example of how to implement a custom file handler.

Define your configurations in the app environment:

```env
app.file.validate.{domain}.db.table: The database table to insert data into.
app.file.validate.{domain}.csv_table_mapping: The CSV to database column mapping file.
app.file.validate.{domain}.file_handler: The custom CSV file handler, if needed.
app.file.validate.payment.file_handler_class: Custom CsvFileHandler class name.
```

### Example Configuration

To use your custom handler, update your application configuration:

```ini
app.file.validate.payment.file_handler = app/payment/payment_csv_file_handler.py
```

Here is Configuration for the payment domain

```ini
# Configuration for the payment domain
app.file.validate.payment.db.table = domain_payment_data
app.file.validate.payment.csv_table_mapping = conf/payment_csv_table_columns_mapping.csv
app.file.validate.payment.file_handler = app/payment/payment_csv_file_handler.py
app.file.validate.payment.file_handler_class = PaymentCsvFileHandler
```

### Example PaymentCsvFileHandler

The `PaymentCsvFileHandler` demonstrates how to extend the functionality of `DefaultCsvFileHandler` to include custom
data conversion logic. By creating a custom handler, you can tailor the data conversion process to meet specific
requirements not covered by the default handler.

```python
class PaymentCsvFileHandler(DefaultCsvFileHandler):
    """ Concrete implementation of DefaultCsvFileHandler for the payment domain """

    def __init__(self, landing_directory, archive_directory):
        # Call the superclass __init__ method
        super().__init__(landing_directory, archive_directory)

    def load_file(self, file_path, workflow_trace_id, batch_id, domain):
        # Custom logic for loading payment CSV files
        logger.info("PaymentCsvFileHandler -> Start processing payment CSV file")
        super().load_file(file_path, workflow_trace_id, batch_id, domain)

```

By following this pattern, you can easily extend and customize the CSV to database conversion process to suit your
application's unique needs.

Feel free to make any additional changes or ask for further customization!
