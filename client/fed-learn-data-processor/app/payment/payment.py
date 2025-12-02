class PaymentData:
    def __init__(self, batch_id, payment_id, amount, currency, txn_date_ts,
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
                 num_txns_rev_dir_1y, avg_gap_txns_same_dir_1y, avg_gap_txns_rev_dir_1y, flag):
        self.batch_id = batch_id
        self.payment_id = payment_id
        self.amount = amount
        self.currency = currency
        self.txn_date_ts = txn_date_ts
        self.crdtr_account_num = crdtr_account_num
        self.crdtr_name = crdtr_name
        self.crdtr_first_name = crdtr_first_name
        self.crdtr_last_name = crdtr_last_name
        self.crdtr_phone = crdtr_phone
        self.crdtr_email = crdtr_email
        self.crdtr_bic_code = crdtr_bic_code
        self.crdtr_address = crdtr_address
        self.crdtr_address1 = crdtr_address1
        self.crdtr_city = crdtr_city
        self.crdtr_state = crdtr_state
        self.crdtr_zipcode = crdtr_zipcode
        self.crdtr_country = crdtr_country
        self.crdtr_account_is_sanction = crdtr_account_is_sanction
        self.crdtr_country_is_sanction = crdtr_country_is_sanction
        self.crdtr_bank_is_sanction = crdtr_bank_is_sanction
        self.crdtr_amount_is_flagged = crdtr_amount_is_flagged
        self.crdtr_diff_threshold = crdtr_diff_threshold
        self.crdtr_std_dev_th_hist = crdtr_std_dev_th_hist
        self.crdtr_email_reputation = crdtr_email_reputation
        self.crdtr_is_phone_active_1m = crdtr_is_phone_active_1m
        self.crdtr_hist_txns_flag_per = crdtr_hist_txns_flag_per
        self.crdtr_age = crdtr_age
        self.crdtr_country_encode = crdtr_country_encode
        self.dbtr_account_num = dbtr_account_num
        self.dbtr_name = dbtr_name
        self.dbtr_first_name = dbtr_first_name
        self.dbtr_last_name = dbtr_last_name
        self.dbtr_phone = dbtr_phone
        self.dbtr_email = dbtr_email
        self.dbtr_bic_code = dbtr_bic_code
        self.dbtr_address = dbtr_address
        self.dbtr_address1 = dbtr_address1
        self.dbtr_city = dbtr_city
        self.dbtr_state = dbtr_state
        self.dbtr_zipcode = dbtr_zipcode
        self.dbtr_country = dbtr_country
        self.dbtr_account_is_sanction = dbtr_account_is_sanction
        self.dbtr_country_is_sanction = dbtr_country_is_sanction
        self.dbtr_bank_is_sanction = dbtr_bank_is_sanction
        self.dbtr_amount_is_flagged = dbtr_amount_is_flagged
        self.dbtr_diff_threshold = dbtr_diff_threshold
        self.dbtr_std_dev_th_hist = dbtr_std_dev_th_hist
        self.dbtr_email_reputation = dbtr_email_reputation
        self.dbtr_is_phone_active_1m = dbtr_is_phone_active_1m
        self.dbtr_hist_txns_flag_per = dbtr_hist_txns_flag_per
        self.dbtr_age = dbtr_age
        self.dbtr_country_encode = dbtr_country_encode
        self.is_ccy_sanctioned = is_ccy_sanctioned
        self.num_txns_same_dir_1y = num_txns_same_dir_1y
        self.num_txns_rev_dir_1y = num_txns_rev_dir_1y
        self.avg_gap_txns_same_dir_1y = avg_gap_txns_same_dir_1y
        self.avg_gap_txns_rev_dir_1y = avg_gap_txns_rev_dir_1y
        self.flag = flag