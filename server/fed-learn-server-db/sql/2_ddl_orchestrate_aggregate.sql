use fedlearn_orchestrator_aggregator;
CREATE TABLE IF NOT EXISTS `domains` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `label` varchar(100) NOT NULL,
    `status` varchar(15) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS model_definition (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `model_name` varchar(255) NOT NULL,
    `model_desc` varchar(255) NOT NULL,
    `model_definition` TEXT,
    `model_version` INT,
    `model_type_id` INT,
    `domain` VARCHAR(50) NOT NULL,
    `status` varchar(15) NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS model_types (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `type_name` VARCHAR(50) NOT NULL,
    `description` varchar(500)
);
CREATE TABLE IF NOT EXISTS metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    source VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    value decimal(15, 10) NOT NULL
);
CREATE TABLE IF NOT EXISTS client (
    id INT NOT NULL AUTO_INCREMENT,
    client_id INT NOT NULL,
    client_name VARCHAR(100) NOT NULL,
    client_email VARCHAR(50) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    consent_record TINYINT(1) NOT NULL DEFAULT 0,
    compliance_status TINYINT(1) NOT NULL DEFAULT 1,
    status varchar(15) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS vendors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status varchar(15) NOT NULL,
    vendor_info VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS model_client_training_result (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    client_workflow_trace_id VARCHAR(500) NOT NULL,
    client_id INT,
    model_id INT NOT NULL,
    domain VARCHAR(50) NOT NULL,
    parameters BLOB NOT NULL,
    checksum CHAR(64) DEFAULT NULL,
    -- Making checksum optional
    loss decimal(15, 10) NOT NULL,
    num_examples INT NOT NULL,
    created_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS model_aggregate_weights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    model_id INT NOT NULL,
    version INT NOT NULL,
    parameters BLOB NOT NULL,
    checksum CHAR(64) DEFAULT NULL,
    -- Making checksum optional
    created_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS aggregate_strategy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    func VARCHAR(255) NOT NULL,
    status varchar(15) NOT NULL,
    vendor_id INT,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);
CREATE TABLE IF NOT EXISTS model_aggregate (
    id INT NOT NULL AUTO_INCREMENT,
    model_id INT NOT NULL,
    strategy_id INT,
    status varchar(15) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (strategy_id) REFERENCES aggregate_strategy(id)
);
CREATE TABLE run_model (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model_id INT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    participants_number INT NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS model_collaboration_run (
    id INT NOT NULL AUTO_INCREMENT,
    model_id INT,
    run_model_id INT,
    group_hash VARCHAR(500) NOT NULL,
    rounds INT NOT NULL,
    current_round INT DEFAULT 0,
    min_clients INT NOT NULL,
    status varchar(15) NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (run_model_id) REFERENCES run_model(id)
);
CREATE TABLE IF NOT EXISTS collaboration_run_client (
    id INT NOT NULL AUTO_INCREMENT,
    run_id INT,
    client_id INT,
    group_hash VARCHAR(500) NOT NULL,
    rounds INT DEFAULT 1,
    workflow_trace_id VARCHAR(500) NOT NULL,
    client_workflow_trace_id VARCHAR(500) NOT NULL,
    min_clients_per_round INT NOT NULL,
    is_submitted BOOLEAN DEFAULT FALSE,
    is_round_complete BOOLEAN DEFAULT FALSE,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (run_id) REFERENCES model_collaboration_run(id),
    FOREIGN KEY (client_id) REFERENCES client(id),
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS run_model_aggregation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    client_id VARCHAR(500) NOT NULL,
    model_id INT NOT NULL,
    group_hash VARCHAR(500) NOT NULL,
    model_weights_id INT,
    loss decimal(15, 10) NOT NULL,
    num_examples INT NOT NULL,
    status varchar(15) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (model_weights_id) REFERENCES model_aggregate_weights(id) ON DELETE
    SET NULL
);
-- orchestrate workflow (both aggregator and agent side)
CREATE TABLE IF NOT EXISTS `workflow_type` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(30),
    `desc` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `workflow` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `model_id` INT NOT NULL,
    `workflow_type_id` int NOT NULL,
    `current_step` int NOT NULL,
    `status` varchar(15) NOT NULL,
    `model_version` INT DEFAULT 0,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`workflow_trace_id`),
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS `workflow_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_id` bigint NOT NULL,
    `workflow_trace_id` VARCHAR(500) NOT NULL,
    `step` int NOT NULL,
    `step_desc` varchar(50) NOT NULL,
    `label` varchar(200) NOT NULL,
    `event` varchar(50) NOT NULL,
    `source` varchar(50) NOT NULL,
    `target` varchar(50) NOT NULL,
    `status` varchar(15) NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`workflow_id`) REFERENCES `workflow`(`id`) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS global_model_weights (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `model_id` bigint NOT NULL,
    `domain` VARCHAR(50) NOT NULL,
    `global_model_weights` BLOB,
    `global_weights_version` INT,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE `user` (
    `id` bigint NOT NULL,
    `name` varchar(100) NOT NULL,
    `email` varchar(100) NOT NULL,
    `nickname` varchar(50),
    `status` varchar(10) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_email` (`email`)
);