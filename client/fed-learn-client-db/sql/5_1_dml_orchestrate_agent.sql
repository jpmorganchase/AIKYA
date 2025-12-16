use fedlearn_orchestrator_agent;

INSERT INTO model_definition (
        model_name,
        model_desc,
        model_version,
        model_type_id,
        status,
        domain
    )
VALUES (
        'credit_card_fraud',
        'Model for processing credit_card_fraud transactions. A neural network model for binary classification using sigmoid.',
        1,
        1,
        'active',
        'credit_card_fraud'
    ) ON DUPLICATE KEY
UPDATE model_name = model_name;
INSERT INTO model_definition (
        model_name,
        model_desc,
        model_version,
        model_type_id,
        status,
        domain
    )
VALUES (
        'payment_fraud',
        'Model for processing payment fraud transactions. A neural network model for binary classification using sigmoid.',
        1,
        1,
        'active',
        'payment_fraud'
    ) ON DUPLICATE KEY
UPDATE model_name = model_name;
INSERT INTO workflow_type (name, `desc`)
VALUES('payment', 'payment model workflow type');
INSERT INTO workflow_type (name, `desc`)
VALUES(
        'credit_card_fraud',
        'credit card fraud model workflow type'
    );
INSERT INTO workflow_type (name, `desc`)
VALUES(
        'payment_fraud',
        'payment fraud model v2 workflow type'
    );
