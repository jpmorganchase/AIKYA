use fedlearn_client;
INSERT INTO data_seed_metadata (
        domain_type,
        batch_id,
        file_name,
        label,
        anomaly_desc,
        is_mock_data
    )
values (
        "payment_fraud",
        'bdf4c816bd83',
        "bank1_[no_fraud]_[app_frac_1]_[no_overlap]_[1].csv",
        "Batch 1",
        "No Anomalies",
        false
    ),
    (
        "payment_fraud",
        'bdf4c816bd83',
        "bank1_[no_fraud]_[app_frac_1]_[no_overlap]_[2].csv",
        "Batch 2",
        "No Anomalies",
        false
    );
INSERT INTO data_seed_metadata (
        domain_type,
        batch_id,
        file_name,
        label,
        anomaly_desc,
        is_mock_data
    )
values (
        "payment_fraud",
        '9a35ca9e5ab5',
        "bank1_[type1]_[app_frac_0.9]_[no_overlap]_[eval_1].csv",
        "Batch 1",
        "Location",
        false
    ),
    (
        "payment_fraud",
        'eedbefe38381',
        "bank1_[type1]_[app_frac_0.9]_[no_overlap]_[eval_2].csv",
        "Batch 2",
        "Location",
        false
    ),
    (
        "payment_fraud",
        '9035f773b4a5',
        "bank1_[type1]_[app_frac_0.9]_[no_overlap]_[eval_3].csv",
        "Batch 3",
        "Location",
        false
    ),
    (
        "payment_fraud",
        'f4a85a486f85',
        "bank1_[type1]_[app_frac_0.9]_[no_overlap]_[eval_4].csv",
        "Batch 4",
        "Location",
        false
    ),
    (
        "payment_fraud",
        'b40ca8750f45',
        "bank1_[type1]_[app_frac_0.9]_[no_overlap]_[eval_5].csv",
        "Batch 5",
        "Location",
        false
    );
INSERT INTO data_seed_metadata (
        domain_type,
        batch_id,
        file_name,
        label,
        anomaly_desc,
        is_mock_data
    )
values (
        "payment_fraud",
        'f34b3b66186d',
        "bank1_[type2]_[app_frac_0.9]_[no_overlap]_[eval_1].csv",
        "Batch 1",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        'de8d74c14734',
        "bank1_[type2]_[app_frac_0.9]_[no_overlap]_[eval_2].csv",
        "Batch 2",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        '41ef0aee45d3',
        "bank1_[type2]_[app_frac_0.9]_[no_overlap]_[eval_3].csv",
        "Batch 3",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        'ab6ff050bf1a',
        "bank1_[type2]_[app_frac_0.9]_[no_overlap]_[eval_4].csv",
        "Batch 4",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        'ea6c0e6692e5',
        "bank1_[type2]_[app_frac_0.9]_[no_overlap]_[eval_5].csv",
        "Batch 5",
        "Account Age",
        false
    );
-- INSERT INTO data_seed_metadata (
--         domain_type,
--         batch_id,
--         file_name,
--         label,
--         anomaly_desc,
--         is_mock_data
--     )
-- values (
--         "payment_fraud",
--         'cfe0219a703b',
--         "bank1_[type1_type2]_[app_frac_0.9]_[no_overlap]_[eval_1].csv",
--         "Batch 1",
--         "Location, Account Age, and Amount",
--         false
--     );