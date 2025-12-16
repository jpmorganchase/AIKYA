-- drop databases
DROP DATABASE IF EXISTS fedlearn_orchestrator_agent;
DROP DATABASE IF EXISTS fedlearn_client;
-- create databases
CREATE DATABASE IF NOT EXISTS `fedlearn_orchestrator_agent`;
CREATE DATABASE IF NOT EXISTS `fedlearn_client`;
-- create root user and grant rights
CREATE USER IF NOT EXISTS 'aikya' @'%' IDENTIFIED BY 'aikya';
GRANT ALL PRIVILEGES ON *.* TO 'aikya' @'%';
-- add more user credentail if need more ....
