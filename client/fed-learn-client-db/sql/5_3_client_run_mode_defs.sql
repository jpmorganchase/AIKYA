use fedlearn_orchestrator_agent;
INSERT INTO client_run_mode (mode, `domain`, name)
VALUES('manual', 'credit_card_fraud', 'workflow');
INSERT INTO client_run_mode (mode, `domain`, name)
VALUES('auto', 'credit_card_fraud', 'version');
INSERT INTO client_run_mode (mode, `domain`, name)
VALUES('manual', 'payment_fraud', 'workflow');
INSERT INTO client_run_mode (mode, `domain`, name)
VALUES('auto', 'payment_fraud', 'version');
