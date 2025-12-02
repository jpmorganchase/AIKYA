use fedlearn_orchestrator_aggregator;
INSERT INTO domains (name, label, status)
values (
        'credit_card_fraud',
        'Credit Card',
        'Active'
    );
INSERT INTO domains (name, label, status)
values (
        'payment_fraud',
        'Payment',
        'Active'
    );
INSERT INTO model_definition (
        model_name,
        model_definition,
        model_desc,
        model_version,
        model_type_id,
        status,
        domain
    )
VALUES (
        'payment',
        '{"module": "keras.src.models.sequential", "class_name": "Sequential", "config": {"name": "sequential", "trainable": true, "dtype": "float32", "layers": [{"module": "keras.src.layers.core.input_layer", "class_name": "InputLayer", "config": {"batch_shape": [null, 27], "dtype": "float32", "sparse": false, "name": "input_layer"}, "registered_name": "InputLayer"}, {"module": "keras.src.layers.core.dense", "class_name": "Dense", "config": {"name": "dense", "trainable": true, "dtype": "float32", "units": 32, "activation": "relu", "use_bias": true, "kernel_initializer": {"module": "keras.src.initializers.random_initializers", "class_name": "GlorotUniform", "config": {"seed": null}, "registered_name": "GlorotUniform"}, "bias_initializer": {"module": "keras.src.initializers.constant_initializers", "class_name": "Zeros", "config": {}, "registered_name": "Zeros"}, "kernel_regularizer": null, "bias_regularizer": null, "kernel_constraint": null, "bias_constraint": null}, "registered_name": "Dense", "build_config": {"input_shape": [null, 27]}}, {"module": "keras.src.layers.core.dense", "class_name": "Dense", "config": {"name": "dense_1", "trainable": true, "dtype": "float32", "units": 64, "activation": "relu", "use_bias": true, "kernel_initializer": {"module": "keras.src.initializers.random_initializers", "class_name": "GlorotUniform", "config": {"seed": null}, "registered_name": "GlorotUniform"}, "bias_initializer": {"module": "keras.src.initializers.constant_initializers", "class_name": "Zeros", "config": {}, "registered_name": "Zeros"}, "kernel_regularizer": null, "bias_regularizer": null, "kernel_constraint": null, "bias_constraint": null}, "registered_name": "Dense", "build_config": {"input_shape": [null, 32]}}, {"module": "keras.src.layers.core.dense", "class_name": "Dense", "config": {"name": "dense_2", "trainable": true, "dtype": "float32", "units": 2, "activation": "softmax", "use_bias": true, "kernel_initializer": {"module": "keras.src.initializers.random_initializers", "class_name": "GlorotUniform", "config": {"seed": null}, "registered_name": "GlorotUniform"}, "bias_initializer": {"module": "keras.src.initializers.constant_initializers", "class_name": "Zeros", "config": {}, "registered_name": "Zeros"}, "kernel_regularizer": null, "bias_regularizer": null, "kernel_constraint": null, "bias_constraint": null}, "registered_name": "Dense", "build_config": {"input_shape": [null, 64]}}], "build_input_shape": [null, 27]}, "registered_name": "Sequential", "build_config": {"input_shape": [null, 27]}, "compile_config": {"optimizer": "adam", "loss": {"module": "keras.src.losses.losses", "class_name": "SparseCategoricalCrossentropy", "config": {"name": "sparse_categorical_crossentropy", "reduction": "sum_over_batch_size", "from_logits": true, "ignore_class": null}, "registered_name": "SparseCategoricalCrossentropy"}, "loss_weights": null, "metrics": ["accuracy"], "weighted_metrics": null, "run_eagerly": false, "steps_per_execution": 1, "jit_compile": false}}',
        'Model for processing payment transactions. A neural network model for binary classification using softmax.',
        1,
        1,
        'active',
        'payment'
    ) ON DUPLICATE KEY
UPDATE model_name = model_name;
INSERT INTO model_definition (
        model_name,
        model_definition,
        model_desc,
        model_version,
        model_type_id,
        status,
        domain
    )
VALUES (
        'credit_card_fraud',
        '{"module": "keras.src.models.sequential", "class_name": "Sequential", "config": {"name": "sequential_1", "trainable": true, "dtype": "float32", "layers": [{"module": "keras.src.layers.core.input_layer", "class_name": "InputLayer", "config": {"batch_shape": [null, 29], "dtype": "float32", "sparse": false, "name": "input_layer_1"}, "registered_name": "InputLayer"}, {"module": "keras.src.layers.core.dense", "class_name": "Dense", "config": {"name": "dense_3", "trainable": true, "dtype": "float32", "units": 16, "activation": "relu", "use_bias": true, "kernel_initializer": {"module": "keras.src.initializers.random_initializers", "class_name": "GlorotUniform", "config": {"seed": null}, "registered_name": "GlorotUniform"}, "bias_initializer": {"module": "keras.src.initializers.constant_initializers", "class_name": "Zeros", "config": {}, "registered_name": "Zeros"}, "kernel_regularizer": null, "bias_regularizer": null, "kernel_constraint": null, "bias_constraint": null}, "registered_name": "Dense", "build_config": {"input_shape": [null, 29]}}, {"module": "keras.src.layers.core.dense", "class_name": "Dense", "config": {"name": "dense_4", "trainable": true, "dtype": "float32", "units": 8, "activation": "relu", "use_bias": true, "kernel_initializer": {"module": "keras.src.initializers.random_initializers", "class_name": "GlorotUniform", "config": {"seed": null}, "registered_name": "GlorotUniform"}, "bias_initializer": {"module": "keras.src.initializers.constant_initializers", "class_name": "Zeros", "config": {}, "registered_name": "Zeros"}, "kernel_regularizer": null, "bias_regularizer": null, "kernel_constraint": null, "bias_constraint": null}, "registered_name": "Dense", "build_config": {"input_shape": [null, 16]}}, {"module": "keras.src.layers.core.dense", "class_name": "Dense", "config": {"name": "dense_5", "trainable": true, "dtype": "float32", "units": 1, "activation": "sigmoid", "use_bias": true, "kernel_initializer": {"module": "keras.src.initializers.random_initializers", "class_name": "GlorotUniform", "config": {"seed": null}, "registered_name": "GlorotUniform"}, "bias_initializer": {"module": "keras.src.initializers.constant_initializers", "class_name": "Zeros", "config": {}, "registered_name": "Zeros"}, "kernel_regularizer": null, "bias_regularizer": null, "kernel_constraint": null, "bias_constraint": null}, "registered_name": "Dense", "build_config": {"input_shape": [null, 8]}}], "build_input_shape": [null, 29]}, "registered_name": "Sequential", "build_config": {"input_shape": [null, 29]}, "compile_config": {"optimizer": "adam", "loss": "binary_crossentropy", "loss_weights": null, "metrics": ["accuracy"], "weighted_metrics": null, "run_eagerly": false, "steps_per_execution": 1, "jit_compile": false}}',
        'Model for processing credit_card_fraud transactions. A neural network model for binary classification using softmax.',
        1,
        1,
        'active',
        'credit_card_fraud'
    ) ON DUPLICATE KEY
UPDATE model_name = model_name;
INSERT INTO model_definition (
        model_name,
        model_definition,
        model_desc,
        model_version,
        model_type_id,
        status,
        domain
    )
VALUES (
        'payment_fraud',
        null,
        'Model for processing payment transactions frauds. A neural network model for binary classification using sigmoid.',
        1,
        1,
        'active',
        'payment_fraud'
    ) ON DUPLICATE KEY
UPDATE model_name = model_name;
INSERT INTO vendors (name, description, status, vendor_info)
VALUES (
        'Flower',
        'A unified approach to federated learning, analytics, and evaluation. Federate any workload, any ML framework, and any programming language.',
        'active',
        'https://flower.ai/'
    );
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
        'payment fraud model workflow type'
    );
insert into model_types (type_name, description)
values (
        'Supervised',
        'Classification,Models that predict a categorical label'
    );
insert into model_types (type_name, description)
values (
        'Supervised',
        'Regression,Models that predict a continuous value'
    );
insert into model_types (type_name, description)
values (
        'Unsupervised',
        'Clustering,Models that group a set of objects into clusters'
    );
insert into model_types (type_name, description)
values (
        'Unsupervised',
        'Dimensionality Reduction,Models that reduce the number of random variables'
    );
insert into model_types (type_name, description)
values (
        'Semi-supervised',
        'Models that use both labeled and unlabeled data'
    );
insert into model_types (type_name, description)
values (
        'Reinforcement',
        'Models that learn to make decisions'
    );
insert into model_types (type_name, description)
values (
        'Deep Learning',
        'CNN,Convolutional Neural Networks for image data'
    );
insert into model_types (type_name, description)
values (
        'Deep Learning',
        'RNN,Recurrent Neural Networks for sequence data'
    );
insert into model_types (type_name, description)
values (
        'Deep Learning',
        'Transformers,"Transformers for handling sequences, particularly in NLP'
    );
insert into model_types (type_name, description)
values (
        'Deep Learning',
        'Autoencoders,Autoencoders for learning efficient codings'
    );
insert into model_types (type_name, description)
values (
        'Deep Learning',
        'GANs,Generative Adversarial Networks for generating new data instances'
    );
insert into model_types (type_name, description)
values (
        'Time Series Analysis',
        'Models specialized for time-dependent data'
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Single',
        'A run payment mode designed for a single participant.',
        1
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-2',
        'A collaborative run payment mode designed for two participants.',
        2
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-3',
        'A collaborative run payment mode designed for three participants.',
        3
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-4',
        'A collaborative run payment mode designed for four participants.',
        4
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-5',
        'A collaborative run payment mode designed for four participants.',
        5
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-6',
        'A collaborative run payment mode designed for four participants.',
        6
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-7',
        'A collaborative run payment mode designed for four participants.',
        7
    );
INSERT INTO run_model (model_id, name, description, participants_number)
VALUES (
        1,
        'Multi-8',
        'A collaborative run payment mode designed for four participants.',
        8
    );
INSERT INTO aggregate_strategy (name, description, func, status, vendor_id)
VALUES (
        'Bulyan',
        'Description for Bulyan',
        'Bulyan',
        'inactive',
        1
    ),
    (
        'Differential Privacy Server Side Adaptive Clipping',
        'Description for Differential Privacy Server Side Adaptive Clipping',
        'DifferentialPrivacyServerSideAdaptiveClipping',
        'inactive',
        1
    ),
    (
        'Differential Privacy Server Side Fixed Clipping',
        'Description for Differential Privacy Server Side Fixed Clipping',
        'DifferentialPrivacyServerSideFixedClipping',
        'inactive',
        1
    ),
    (
        'DP Fed Avg Adaptive',
        'Description for DP Fed Avg Adaptive',
        'DPFedAvgAdaptive',
        'inactive',
        1
    ),
    (
        'DP Fed Avg Fixed',
        'Description for DP Fed Avg Fixed',
        'DPFedAvgFixed',
        'inactive',
        1
    ),
    (
        'Fault Tolerant Fed Avg',
        'Description for Fault Tolerant Fed Avg',
        'FaultTolerantFedAvg',
        'inactive',
        1
    ),
    (
        'Fed Adagrad',
        'Description for Fed Adagrad',
        'FedAdagrad',
        'inactive',
        1
    ),
    (
        'Fed Adam',
        'Description for Fed Adam',
        'FedAdam',
        'inactive',
        1
    ),
    (
        'Fed Avg Android',
        'Description for Fed Avg Android',
        'FedAvgAndroid',
        'inactive',
        1
    ),
    (
        'FedAvg',
        'FedAvg is a widely used strategy in federated learning where the server aggregates model updates from clients by averaging them. This method was introduced by McMahan et al. in 2016.',
        'FedAvg',
        'active',
        1
    ),
    (
        'Fed Avg M',
        'Description for Fed Avg M',
        'FedAvgM',
        'active',
        1
    ),
    (
        'Fed Median',
        'Description for Fed Median',
        'FedMedian',
        'active',
        1
    ),
    (
        'Fed Opt',
        'Description for Fed Opt',
        'FedOpt',
        'inactive',
        1
    ),
    (
        'Fed Prox',
        'Description for Fed Prox',
        'FedProx',
        'inactive',
        1
    ),
    (
        'Fed Trimmed Avg',
        'Description for Fed Trimmed Avg',
        'FedTrimmedAvg',
        'active',
        1
    ),
    (
        'Fed Xgb Bagging',
        'Description for Fed Xgb Bagging',
        'FedXgbBagging',
        'inactive',
        1
    ),
    (
        'Fed Xgb Cyclic',
        'Description for Fed Xgb Cyclic',
        'FedXgbCyclic',
        'inactive',
        1
    ),
    (
        'Fed Xgb Nn Avg',
        'Description for Fed Xgb Nn Avg',
        'FedXgbNnAvg',
        'inactive',
        1
    ),
    (
        'Fed Yogi',
        'Description for Fed Yogi',
        'FedYogi',
        'inactive',
        1
    ),
    (
        'Krum',
        'Description for Krum',
        'Krum',
        'inactive',
        1
    ),
    (
        'Q Fed Avg',
        'Description for Q Fed Avg',
        'QFedAvg',
        'inactive',
        1
    );
INSERT INTO fedlearn_orchestrator_aggregator.global_model_weights (
        model_id,
        `domain`,
        global_model_weights,
        global_weights_version,
        created_date
    )
values(
        1,
        'payment',
        "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",
        1,
        CURRENT_TIMESTAMP
    ),
    (
        2,
        'credit_card_fraud',
        "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",
        1,
        CURRENT_TIMESTAMP
    );