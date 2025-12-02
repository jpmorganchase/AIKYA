import csv

from app.payment.payment import PaymentData
from src.service.file.csv_file_handler import CsvFileHandler
import logging as logger
from src.util.app_utils import get_random_id, convert_to_decimal_value
from src.repository.base_repository import execute_batch_insert


# logger = log.init_logger()


class PaymentCsvFileHandler(CsvFileHandler):
    """ Concrete implementation of CsvFileHandlerTemplate """

    def __init__(self, landing_directory, archive_directory):
        # Call the superclass __init__ method
        super().__init__(landing_directory, archive_directory)

    def load_file(self, file_path, workflow_trace_id, batch_id, domain):
        """
        Load data from a CSV file.

        This method should be implemented in subclasses to load data from the CSV file
        and perform any necessary processing.

        Parameters:
            file_path (str): The path to the CSV file to be loaded.
            workflow_trace_id (str): The workflow Trace ID
            batch_id (str): The batch_id for file batch_process
            domain: payment
        """
        logger.info("Start processing load Payment CSV file: : {0}".format(file_path))
        payments = []
        try:
            with open(file_path, mode='r', newline='', encoding='utf-8') as file:
                csv_reader = csv.DictReader(file)
                for row in csv_reader:
                    # Create a PaymentData object for each row
                    # Convert directly and ensure it's not a tuple
                    # Debug print to check the value
                    payment = PaymentData(
                        batch_id=batch_id,
                        amount=int(row['amount']),
                        currency=row['currency'],
                        txn_date_ts=row['txn_date_ts'],
                        crdtr_account_num=row['crdtr_account_num'],
                        crdtr_name=row['crdtr_name'],
                        crdtr_first_name='',
                        crdtr_last_name='',
                        crdtr_phone=row['crdtr_phone'],
                        crdtr_email=row['crdtr_email'],
                        crdtr_bic_code=row['crdtr_bic_code'],
                        crdtr_address=row['crdtr_address'],
                        crdtr_address1='',
                        crdtr_city=row['crdtr_city'],
                        crdtr_state='',
                        crdtr_zipcode=row['crdtr_zipcode'],
                        crdtr_country=row['crdtr_country'],
                        crdtr_account_is_sanction=int(row.get('crdtr_account_is_sanction', 0)),
                        crdtr_country_is_sanction=int(row.get('crdtr_country_is_sanction', 0)),
                        crdtr_bank_is_sanction=int(row.get('crdtr_bank_is_sanction', 0)),
                        crdtr_amount_is_flagged=int(row.get('crdtr_amount_is_flagged', 0)),
                        crdtr_diff_threshold=convert_to_decimal_value(row.get('crdtr_diff_threshold'), '0.00'),
                        crdtr_std_dev_th_hist=convert_to_decimal_value(row.get('crdtr_std_dev_th_hist'), '0.00'),
                        crdtr_email_reputation=convert_to_decimal_value(row.get('crdtr_email_reputation'), '0.00'),
                        crdtr_is_phone_active_1m=int(row.get('crdtr_is_phone_active_1m', 0)),
                        crdtr_hist_txns_flag_per=int(row.get('crdtr_hist_txns_flag_per', 0)),
                        crdtr_age=int(row.get('crdtr_age', 0)),
                        crdtr_country_encode=int(row.get('crdtr_country_encode', 0)),

                        # Repeat the same for debtor fields

                        dbtr_account_num=row['dbtr_account_num'],
                        dbtr_name=row['dbtr_name'],
                        dbtr_first_name='',
                        dbtr_last_name='',
                        dbtr_phone=row['dbtr_phone'],
                        dbtr_email=row['dbtr_email'],
                        dbtr_bic_code=row['dbtr_bic_code'],
                        dbtr_address=row['dbtr_address'],
                        dbtr_address1='',
                        dbtr_city=row['dbtr_city'],
                        dbtr_state='',
                        dbtr_zipcode=row['dbtr_zipcode'],
                        dbtr_country=row['dbtr_country'],
                        dbtr_account_is_sanction=int(row.get('dbtr_account_is_sanction', 0)),
                        dbtr_country_is_sanction=int(row.get('dbtr_country_is_sanction', 0)),
                        dbtr_bank_is_sanction=int(row.get('dbtr_bank_is_sanction', 0)),
                        dbtr_amount_is_flagged=int(row.get('dbtr_amount_is_flagged', 0)),
                        dbtr_diff_threshold=convert_to_decimal_value(row.get('dbtr_diff_threshold'), '0.00'),
                        dbtr_std_dev_th_hist=convert_to_decimal_value(row.get('dbtr_std_dev_th_hist'), '0.00'),
                        dbtr_email_reputation=convert_to_decimal_value(row.get('dbtr_email_reputation'), '0.00'),
                        dbtr_is_phone_active_1m=int(row.get('dbtr_is_phone_active_1m', 0)),
                        dbtr_hist_txns_flag_per=int(row.get('dbtr_hist_txns_flag_per', 0)),
                        dbtr_age=int(row.get('dbtr_age', 0)),
                        dbtr_country_encode=int(row.get('dbtr_country_encode', 0)),

                        is_ccy_sanctioned=int(row.get('is_ccy_sanctioned', 0)),
                        num_txns_same_dir_1y=int(row.get('num_txns_same_dir_1y', 0)),
                        num_txns_rev_dir_1y=int(row.get('num_txns_rev_dir_1y', 0)),
                        avg_gap_txns_same_dir_1y=int(row.get('avg_gap_txns_same_dir_1y', 0)),
                        avg_gap_txns_rev_dir_1y=int(row.get('avg_gap_txns_rev_dir_1y', 0)),
                        flag=int(row.get('flag', 0))
                    )
                    payments.append(payment)

            if payments:
                save_payment_datas(payments, workflow_trace_id)
            # If processing is successful, move the file to the archive folder
            logger.info("Completed load CSV file: workflow_trace_id: {0}, file_path: {1}, batch_id: {2}".format(
                workflow_trace_id,
                file_path,
                batch_id))
            return True
        except Exception as e:
            # If an error occurs during processing, handle the error
            logger.error("Error load CSV file: : {0}".format(e))
            return False


def save_payment_datas(payments, workflow_trace_id):
    sql_insert_query = """
       INSERT INTO domain_payment_data (
           batch_id, amount, currency, txn_date_ts,
           crdtr_account_num, crdtr_name, crdtr_first_name, crdtr_last_name,
           crdtr_phone, crdtr_email, crdtr_bic_code, crdtr_address,
           crdtr_address1, crdtr_city, crdtr_state, crdtr_zipcode, crdtr_country,
           crdtr_account_is_sanction, crdtr_country_is_sanction, crdtr_bank_is_sanction,
           crdtr_amount_is_flagged, crdtr_diff_threshold, crdtr_std_dev_th_hist,
           crdtr_email_reputation, crdtr_is_phone_active_1m, crdtr_hist_txns_flag_per,
           crdtr_age, crdtr_country_encode, dbtr_account_num, dbtr_name, dbtr_first_name,
           dbtr_last_name, dbtr_phone, dbtr_email, dbtr_bic_code, dbtr_address,
           dbtr_address1, dbtr_city, dbtr_state, dbtr_zipcode, dbtr_country,
           dbtr_account_is_sanction, dbtr_country_is_sanction, dbtr_bank_is_sanction,
           dbtr_amount_is_flagged, dbtr_diff_threshold, dbtr_std_dev_th_hist,
           dbtr_email_reputation, dbtr_is_phone_active_1m, dbtr_hist_txns_flag_per,
           dbtr_age, dbtr_country_encode, is_ccy_sanctioned, num_txns_same_dir_1y,
           num_txns_rev_dir_1y, avg_gap_txns_same_dir_1y, avg_gap_txns_rev_dir_1y, flag)
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                  %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                  %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
       """
    # Prepare a list of tuples from the data attributes
    values = [
        (d.batch_id, d.amount, d.currency, d.txn_date_ts,
         d.crdtr_account_num, d.crdtr_name, d.crdtr_first_name, d.crdtr_last_name,
         d.crdtr_phone, d.crdtr_email, d.crdtr_bic_code, d.crdtr_address,
         d.crdtr_address1, d.crdtr_city, d.crdtr_state, d.crdtr_zipcode, d.crdtr_country,
         d.crdtr_account_is_sanction, d.crdtr_country_is_sanction, d.crdtr_bank_is_sanction,
         d.crdtr_amount_is_flagged, d.crdtr_diff_threshold, d.crdtr_std_dev_th_hist,
         d.crdtr_email_reputation, d.crdtr_is_phone_active_1m, d.crdtr_hist_txns_flag_per,
         d.crdtr_age, d.crdtr_country_encode, d.dbtr_account_num, d.dbtr_name, d.dbtr_first_name,
         d.dbtr_last_name, d.dbtr_phone, d.dbtr_email, d.dbtr_bic_code, d.dbtr_address,
         d.dbtr_address1, d.dbtr_city, d.dbtr_state, d.dbtr_zipcode, d.dbtr_country,
         d.dbtr_account_is_sanction, d.dbtr_country_is_sanction, d.dbtr_bank_is_sanction,
         d.dbtr_amount_is_flagged, d.dbtr_diff_threshold, d.dbtr_std_dev_th_hist,
         d.dbtr_email_reputation, d.dbtr_is_phone_active_1m, d.dbtr_hist_txns_flag_per,
         d.dbtr_age, d.dbtr_country_encode, d.is_ccy_sanctioned, d.num_txns_same_dir_1y,
         d.num_txns_rev_dir_1y, d.avg_gap_txns_same_dir_1y, d.avg_gap_txns_rev_dir_1y, d.flag)
        for d in payments
    ]
    total_records = execute_batch_insert(sql_insert_query, values)
    logger.info(
        "workflow_id: {0} - Completed load CSV file to Payment Table, total records: {1}".format(workflow_trace_id,
                                                                                                 total_records))
