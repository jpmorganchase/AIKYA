use fedlearn_orchestrator_agent;
-- orchestrate model (both aggregator and agent side)
CREATE TABLE IF NOT EXISTS model_definition (
    `id` INT NOT NULL AUTO_INCREMENT,
    `model_name` varchar(255) NOT NULL,
    `model_desc` varchar(255),
    `model_definition` TEXT,
    `model_version` INT,
    `model_type_id` INT,
    `domain` VARCHAR(50) NOT NULL,
    `status` varchar(15) NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `last_update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS model_types (
    `id` INT NOT NULL AUTO_INCREMENT,
    `type_name` VARCHAR(50) NOT NULL,
    `description` varchar(500),
    PRIMARY KEY (`id`)
);
-- orchestrate model_weights (save model weight for global weight version)
CREATE TABLE IF NOT EXISTS global_model_weights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    model_id INT NOT NULL,
    is_self char(1) NOT NULL,
    version INT,
    parameters BLOB NOT NULL,
    checksum CHAR(64) DEFAULT NULL,
    -- Making checksum optional
    created_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES model_definition(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS global_metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(500) NOT NULL,
    source VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    value decimal(15, 10) NOT NULL
);
CREATE TABLE IF NOT EXISTS global_model_training_result (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_trace_id VARCHAR(50) NOT NULL,
    model_id INT NOT NULL,
    loss decimal(15, 10) NOT NULL,
    num_examples INT NOT NULL,
    created_date timestamp NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS agent_model_logs (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `model_id` bigint NOT NULL,
    `local_weights_version` INT,
    `global_weights_version` INT,
    PRIMARY KEY (`id`)
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
CREATE TABLE IF NOT EXISTS `client_run_mode` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `mode` varchar(20) NOT NULL,
    `domain` varchar(50) NOT NULL,
    `name` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `workflow_run_mode_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `workflow_run_mode_id` bigint NOT NULL,
    `workflow_trace_id` varchar(500) NOT NULL,
    `workflow_step` int NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`workflow_run_mode_id`) REFERENCES `client_run_mode`(`id`) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS `weight_run_mode_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `weight_run_mode_id` bigint NOT NULL,
    `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`weight_run_mode_id`) REFERENCES `client_run_mode`(`id`) ON DELETE CASCADE
);
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL,
    `email` varchar(100) NOT NULL,
    `nickname` varchar(50),
    `status` varchar(10) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_email` (`email`)
);
CREATE TABLE user_dashboard_preferences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id bigint NOT NULL,
    domain varchar(50) NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    column_preferences JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(`id`) ON DELETE CASCADE
);
