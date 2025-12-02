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
        '956eae310bc0',
        "bank2_[no_fraud]_[app_frac_1]_[no_overlap]_[1].csv",
        "Batch 1",
        "No Anomalies",
        false
    ),
    (
        "payment_fraud",
        '956eae310bc0',
        "bank2_[no_fraud]_[app_frac_1]_[no_overlap]_[2].csv",
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
        'dbf759b3dfb6',
        "bank2_[type2]_[app_frac_0.9]_[no_overlap]_[eval_1].csv",
        "Batch 1",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        '3e3415b5fc36',
        "bank2_[type2]_[app_frac_0.9]_[no_overlap]_[eval_2].csv",
        "Batch 2",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        '54cadead92be',
        "bank2_[type2]_[app_frac_0.9]_[no_overlap]_[eval_3].csv",
        "Batch 3",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        '044367ff1e0d',
        "bank2_[type2]_[app_frac_0.9]_[no_overlap]_[eval_4].csv",
        "Batch 4",
        "Account Age",
        false
    ),
    (
        "payment_fraud",
        'ceb31c594ba4',
        "bank2_[type2]_[app_frac_0.9]_[no_overlap]_[eval_5].csv",
        "Batch 5",
        "Account Age",
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
        '28d45bbc5808',
        "bank2_[type1]_[app_frac_0.9]_[no_overlap]_[eval_1].csv",
        "Batch 1",
        "Location",
        false
    ),
    (
        "payment_fraud",
        '112537a8b6bd',
        "bank2_[type1]_[app_frac_0.9]_[no_overlap]_[eval_2].csv",
        "Batch 2",
        "Location",
        false
    ),
    (
        "payment_fraud",
        'c55a0f918062',
        "bank2_[type1]_[app_frac_0.9]_[no_overlap]_[eval_3].csv",
        "Batch 3",
        "Location",
        false
    ),
    (
        "payment_fraud",
        '24757b97d6c3',
        "bank2_[type1]_[app_frac_0.9]_[no_overlap]_[eval_4].csv",
        "Batch 4",
        "Location",
        false
    ),
    (
        "payment_fraud",
        '3d0393787718',
        "bank2_[type1]_[app_frac_0.9]_[no_overlap]_[eval_5].csv",
        "Batch 5",
        "Location",
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
--         '3f446cb7ab80',
--         "bank2_[type1_type2]_[app_frac_0.9]_[no_overlap]_[eval_1].csv",
--         "Batch 1",
--         "Unfamiliar Location, Account Age, and Amount",
--         false
--     );