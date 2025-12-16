use fedlearn_client;
CREATE TABLE IF NOT EXISTS `data_seed` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_trace_id` varchar(255) NOT NULL,
    `batch_id` varchar(255) NOT NULL,
    `file_path` varchar(255) DEFAULT '',
    `file_name` varchar(100) DEFAULT '',
    `label` varchar(100) DEFAULT '',
    `model` varchar(50) DEFAULT '',
    `domain_type` varchar(20) NOT NULL,
    `is_mock_data` char(1) NOT NULL,
    `status` varchar(15) NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `data_seed_metadata` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `domain_type` varchar(50) NOT NULL,
    `batch_id` varchar(50) NOT NULL,
    `file_name` varchar(100) DEFAULT '',
    `anomaly_desc` varchar(200) DEFAULT '',
    `label` varchar(100) DEFAULT '',
    `is_mock_data` char(1) NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `domains` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `label` varchar(100) NOT NULL,
    `status` varchar(15) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS model_client_records (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `definition` TEXT NOT NULL,
    `model_version` INT,
    `domain` VARCHAR(50) NOT NULL,
    `local_model_weights` BLOB,
    `local_weights_version` INT,
    `global_model_weights` BLOB,
    `global_weights_version` INT,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS model_client_record_history (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `name` varchar(255) NOT NULL,
    `model_weights` BLOB,
    `version` INT,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `model_predict_data` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `domain` varchar(20) NOT NULL,
    `batch_id` varchar(50) NOT NULL,
    `item_id` bigint NOT NULL,
    `result` varchar(50) NOT NULL,
    `confidence_score` double,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `model_predict_shap_data` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `domain` varchar(20) NOT NULL,
    `batch_id` varchar(50) NOT NULL,
    `shapley_values` JSON NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `model_feedback` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `model_data_id` bigint NOT NULL,
    `batch_id` varchar(50) NOT NULL,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `item_id` bigint NOT NULL,
    `score` int NOT NULL,
    `is_correct` char(1) NOT NULL,
    `comment` text,
    `status` int NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`model_data_id`) REFERENCES `model_predict_data` (`id`)
);
CREATE TABLE IF NOT EXISTS `model_data_features` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `domain` varchar(20) NOT NULL,
    `model` varchar(50) NOT NULL,
    `db_table` varchar(50) NOT NULL,
    `id_field` varchar(50) NOT NULL,
    `feature_field` varchar(50) NOT NULL,
    `seq_num` int NOT NULL,
    `status` varchar(15) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS `workflow_model_logs` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `event` varchar(50) NOT NULL,
    `status` varchar(15) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    source VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    value decimal(15, 10) NOT NULL
);
CREATE TABLE IF NOT EXISTS model_training_result (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(50) NOT NULL,
    num_examples INT NOT NULL,
    created_date timestamp NULL DEFAULT CURRENT_TIMESTAMP
);