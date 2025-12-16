use fedlearn_orchestrator_agent;

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