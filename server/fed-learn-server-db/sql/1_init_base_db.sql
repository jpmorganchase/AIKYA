-- drop databases
DROP DATABASE IF EXISTS fedlearn_orchestrator_aggregator;
-- create databases
CREATE DATABASE IF NOT EXISTS `fedlearn_orchestrator_aggregator`;
-- create root user and grant rights
CREATE USER IF NOT EXISTS 'aikya' @'%' IDENTIFIED BY 'aikya';
GRANT ALL PRIVILEGES ON *.* TO 'aikya' @'%';
-- add more user credential if need more ....