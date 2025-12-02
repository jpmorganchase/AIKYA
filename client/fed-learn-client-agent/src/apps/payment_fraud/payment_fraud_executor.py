import math
import os
from os import path
from typing import Union, Any

import keras.src.metrics
import numpy as np
import sklearn
from sklearn.metrics import accuracy_score, precision_score, recall_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, RobustScaler
import pandas as pd
import shap

from src.ml_models.executor.db_executor import DBModelExecutor
from src.util import log

from datetime import datetime
from src.apps.payment_fraud.payment_fraud_model import PAYMENT_FRAUD_MODEL_FEATURE_LIST, PAYMENT_FRAUD_FLAG

from src.repository.model.model_track_repository import save_mode_training_result, \
    create_and_update_model_weight_records, insert_shapley_values
from src.ml_models.tool.model_builder_tool import compress_weights

from src.config import get_config

logger = log.init_logger()


class PaymentFraudExecutor(DBModelExecutor):

    def gather_metrics(self, dataset: pd.DataFrame) -> dict:
        logger.info("Generating metrics for dataset...")
        y_true = dataset.loc[:, PAYMENT_FRAUD_FLAG]
        y_predict = np.round(
            np.ravel(self.model.predict(
                dataset.loc[:, PAYMENT_FRAUD_MODEL_FEATURE_LIST], verbose=2))
        )
        return {
            "accuracy": accuracy_score(y_true, y_predict),
            "precision": precision_score(y_true, y_predict),
            "recall": recall_score(y_true, y_predict),
            "loss": round(float(keras.src.metrics.BinaryCrossentropy()(y_true, y_predict).numpy()), 6)
        }

    def __init__(self, model_context):
        super().__init__(model_context)

        self.scaler_ = None
        self._shapley_explainer: Union[shap.DeepExplainer, None] = None
        payment_fraud_scaling_dataset_dir = path.abspath(
            path.expandvars(
                path.expanduser(get_config(
                    "app.scaling.dataset.payment_fraud"))
            )
        )
        logger.info(
            f"Scaling dataset directory: {payment_fraud_scaling_dataset_dir}"
        )
        if not path.exists(payment_fraud_scaling_dataset_dir) or not path.isdir(payment_fraud_scaling_dataset_dir):
            logger.warning(
                "Dir %s does not exist or it is not a directory. Skipping creating a scaler",
                payment_fraud_scaling_dataset_dir
            )

        logger.info("Loading files from %s as genesis dataset...",
                    payment_fraud_scaling_dataset_dir)
        scaling_dataset: pd.DataFrame = pd.DataFrame()
        for filename in os.listdir(payment_fraud_scaling_dataset_dir):
            file = path.join(payment_fraud_scaling_dataset_dir, filename)
            logger.info(f"Loading data from {file}...")
            if path.exists(file) and path.isfile(file) and file.endswith(".csv"):
                scaling_dataset = pd.concat(
                    [scaling_dataset, pd.read_csv(file)])

        logger.info(
            f"Scaling dataset loaded. Shape: {scaling_dataset.shape}. Preprocessing data...")
        scaling_dataset = self.preprocessing(
            dataset=scaling_dataset, get_flag=False, skip_scaler=True)
        self.scaler_ = sklearn.preprocessing.StandardScaler().fit(scaling_dataset)
        logger.info(f"Dataset scaler created. {self.scaler_}")

    def _load_training_data(self, get_flag: bool = False, subset: Union[int, None] = None) -> pd.DataFrame:
        payment_fraud_genesis_dataset_dir = path.abspath(
            path.expandvars(
                path.expanduser(get_config(
                    "app.genesis.dataset.payment_fraud"))
            )
        )
        logger.info(
            f"Genesis dataset directory: {payment_fraud_genesis_dataset_dir}")
        if not path.exists(payment_fraud_genesis_dataset_dir) or not path.isdir(payment_fraud_genesis_dataset_dir):
            logger.warn(
                "Dir %s does not exist or it is not a directory. Defaulting to base class implementation",
                payment_fraud_genesis_dataset_dir
            )
            return super().run_training()

        # get all genesis training datasets
        logger.info("Loading files from %s as genesis dataset...",
                    payment_fraud_genesis_dataset_dir)
        genesis_dataset: pd.DataFrame = pd.DataFrame()
        for filename in os.listdir(payment_fraud_genesis_dataset_dir):
            file = path.join(payment_fraud_genesis_dataset_dir, filename)
            logger.info(f"Loading data from {file}...")
            if path.exists(file) and path.isfile(file) and file.endswith(".csv"):
                genesis_dataset = pd.concat(
                    [genesis_dataset, pd.read_csv(file)])
        if subset:
            logger.info(f"Truncating to subset {subset}...")
            genesis_dataset = genesis_dataset.iloc[np.random.choice(
                genesis_dataset.shape[0], subset, replace=False), :]

        logger.info(
            f"Genesis dataset loaded. Shape: {genesis_dataset.shape}. Preprocessing data...")
        genesis_dataset = self.preprocessing(genesis_dataset, get_flag)
        logger.info(f"Preprocessed. Shape: {genesis_dataset.shape}.")
        return genesis_dataset

    def run_training(self):
        self.build_model()
        logger.info(
            "----------------------------------- TRAINING: BEGIN -----------------------------------")
        logger.info(f"Running {self.__class__.__name__} training...")
        genesis_dataset: pd.DataFrame = self._load_training_data(True)
        x_train, x_test, y_train, y_test = train_test_split(
            genesis_dataset.loc[:, PAYMENT_FRAUD_MODEL_FEATURE_LIST],
            genesis_dataset.loc[:, PAYMENT_FRAUD_FLAG],
            random_state=42, test_size=0.3, shuffle=True
        )
        logger.info(
            f"""Generated training and prediction splits.
            x_train shape: {x_train.shape}, x_test shape: {x_test.shape},
            y_train shape: {y_train.shape}, y_test shape: {y_test.shape}"""
        )
        training_epochs = 1
        batch_size = 16
        logger.info("Starting training...")
        self.model.fit(x_train.to_numpy(), y_train.to_numpy(), epochs=training_epochs, batch_size=batch_size, verbose=2,
                       validation_split=0.2)
        logger.info("Training finished. Gathering metrics...")
        metrics = self.gather_metrics(
            pd.concat([x_test, y_test], axis=1)
        )
        logger.info("Metrics collected.")

        workflow_trace_id = self.model_context.workflow_trace_id
        num_examples = x_train.shape[0]
        logger.info(
            f"Saving model training result: {self.model_context.workflow_trace_id}, num_examples: {num_examples}")
        save_mode_training_result(workflow_trace_id, num_examples, metrics)
        weights_compressed = compress_weights(self.model.get_weights())
        logger.info(
            f"Saving model weight version records: {workflow_trace_id} ...")
        create_and_update_model_weight_records(
            workflow_trace_id, self.domain(), weights_compressed, self.domain())

        logger.info(
            "----------------------------------- TRAINING: END -----------------------------------")
        return 'success', self.model_context.workflow_trace_id, 0, num_examples, metrics

    def generate_labels_array(self, is_correct_req: pd.Series, result_list: pd.Series, threshold=73.0):
        """
        Generate labels array based on is_correct_req and result_list with a given threshold.

        :param is_correct_req: List of strings indicating whether the request is correct ('Y' or 'N').
        :param result_list: List of floats representing some result values.
        :param threshold: Float value to use as the threshold for labeling.
        :return: NumPy array of labels.
        TODO: Review THIS LOGIC
        """
        logger.info(
            "generate payment fraud labels array, threshold: %s, is_correct_req size: %s, result size: %s",
            threshold, len(is_correct_req), len(result_list)
        )
        y = []
        for i in range(len(result_list)):
            feedback_i = result_list.iloc[i]
            prediction_correct_i = is_correct_req.iloc[i].upper()
            if np.isnan(feedback_i) or math.isnan(feedback_i):
                continue

            match prediction_correct_i:
                case "Y":
                    y.append(result_list[i])
                case "N":
                    flip = math.floor(feedback_i) \
                        if feedback_i < threshold \
                        else math.ceil(feedback_i)
                    y.append(1 - flip)
        labels_array = np.array(y, dtype=np.int64)
        return labels_array

    def name(self):
        return "Payment Fraud DB Executor"

    def preprocessing(self, dataset: pd.DataFrame, get_flag=False, skip_scaler: bool = False) -> pd.DataFrame:
        _dataset = prepare_dataset(
            dataset=dataset, skip_scaler=skip_scaler, scaler=self.scaler_)
        _cols = PAYMENT_FRAUD_MODEL_FEATURE_LIST + \
                ([PAYMENT_FRAUD_FLAG] if get_flag else [])
        return _dataset.loc[:, _cols]

    def shap_feature_explanation(self, pred_data: pd.DataFrame, predictions: np.ndarray):
        genesis_dataset: pd.DataFrame = self._load_training_data(subset=1000)
        logger.info("creating shapley explainer using the trained model...")
        # create a shapley deep explainer using the training dataset required.
        # note that the documentation suggests that we can sample the data source for
        # 100 -> 1000 rows which is enough to setup the integrator
        self._shapley_explainer = shap.DeepExplainer(
            self.model, genesis_dataset.values
        )
        logger.info(
            f"Shapley explainer created. Used {genesis_dataset.shape[0]} background samples")

        subset_idxs = np.random.choice(pred_data.shape[0], min(
            500, pred_data.shape[0]), replace=False)
        logger.info(
            f"Using shapley explainer with prediction subset of size {len(subset_idxs)}")

        subset_pred_dataset = pred_data.iloc[subset_idxs, :].reset_index(
            drop=True)
        subset_predictions = predictions[subset_idxs]

        preds_explainer_values = self._shapley_explainer.shap_values(
            subset_pred_dataset.values)
        all_shapley_explainer_values = []

        for idx, shap_tuple in enumerate(preds_explainer_values):
            feature_shap_values_json = {
                PAYMENT_FRAUD_MODEL_FEATURE_LIST[_]: round(float(shap_value[0]), 5)
                for _, shap_value in enumerate(shap_tuple)
            }
            # feature_shap_values_json["PREDICTION"] = float(subset_predictions[idx][0])
            all_shapley_explainer_values.append(feature_shap_values_json)

        batch_size = 25
        batch_count = math.ceil(len(all_shapley_explainer_values) / batch_size)
        start_idx = 0
        for i in range(batch_count):
            end_idx = start_idx + batch_size
            insert_shapley_values(
                self.model_context.workflow_trace_id,
                self.domain(),
                self.model_context.batch_id,
                all_shapley_explainer_values[
                start_idx:(
                    end_idx if end_idx < len(all_shapley_explainer_values) else len(all_shapley_explainer_values) - 1)
                ]
            )
            start_idx = end_idx
        logger.info("Updated shapley explainer values")


def log_normalize_columns(dataset: pd.DataFrame, fields: list) -> pd.DataFrame:
    dataset.loc[:, fields] = dataset.loc[:, fields].apply(lambda _: np.log(_))
    return dataset


def convert_timestamp_field_to_age(dataset: pd.DataFrame, timestamp_field: str, fields: list) -> pd.DataFrame:
    dataset.loc[:, timestamp_field] = dataset.loc[:, fields].apply(
        lambda _: ((datetime.fromtimestamp(_.iloc[0]) - datetime.fromtimestamp(_.iloc[1])).total_seconds() / 60), axis=1
    )
    return dataset


def get_distance_from_lat_long(dataset: pd.DataFrame, distance_field: str, fields: list) -> pd.DataFrame:
    def calculate_geodesic_distance(lat1, lon1, lat2, lon2) -> np.float32:
        # https://stackoverflow.com/a/19412565
        earth_radius = 3958.8 * 1.6  # in km
        lat1 = math.radians(lat1)
        lon1 = math.radians(lon1)
        lat2 = math.radians(lat2)
        lon2 = math.radians(lon2)
        lat_dist = lat2 - lat1
        lon_dist = lon2 - lon1
        a = math.sin(lat_dist / 2) ** 2 + math.cos(lat1) * \
            math.cos(lat2) * math.sin(lon_dist / 2) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        distance = earth_radius * c
        return np.float64(distance)

    dataset.loc[:, distance_field] = dataset.loc[:, fields].apply(
        lambda row: calculate_geodesic_distance(row.iloc[0], row.iloc[1], row.iloc[2], row.iloc[3]), axis=1
    )
    return dataset


def scale_data(df: pd.DataFrame) -> pd.DataFrame:
    # scaler = StandardScaler()
    scaler = RobustScaler()
    logger.info(f"Using scaler: {scaler}")
    scaled_data = scaler.fit_transform(df)
    return scaled_data


def prepare_dataset(dataset: pd.DataFrame, skip_scaler=False, scaler: any = None) -> pd.DataFrame:
    dataset.columns = dataset.columns.str.upper()
    dataset = convert_timestamp_field_to_age(
        dataset, "DEBITOR_ACCOUNT_AGE",
        ["PAYMENT_INIT_TIMESTAMP", "DEBITOR_ACCOUNT_CREATE_TIMESTAMP"]
    )
    dataset = convert_timestamp_field_to_age(
        dataset, "CREDITOR_ACCOUNT_AGE",
        ["PAYMENT_INIT_TIMESTAMP", "CREDITOR_ACCOUNT_CREATE_TIMESTAMP"]
    )
    dataset = get_distance_from_lat_long(
        dataset, "DEBITOR_PHY_AND_TOWER_DISTANCE",
        ["DEBITOR_GEO_LATITUDE", "DEBITOR_GEO_LONGITUDE",
         "DEBITOR_TOWER_LATITUDE", "DEBITOR_TOWER_LONGITUDE"]
    )
    dataset = get_distance_from_lat_long(
        dataset, "CREDITOR_PHY_AND_TOWER_DISTANCE",
        ["CREDITOR_GEO_LATITUDE", "CREDITOR_GEO_LONGITUDE",
         "CREDITOR_TOWER_LATITUDE", "CREDITOR_TOWER_LONGITUDE"]
    )
    dataset = log_normalize_columns(
        dataset, ["CREDITOR_ACCOUNT_AGE", "DEBITOR_ACCOUNT_AGE"]
    )

    # skip scaling
    if skip_scaler:
        return dataset

    # now all features should be numeric and unit-less. we can scale the data correctly now
    if scaler:
        dataset.loc[:, PAYMENT_FRAUD_MODEL_FEATURE_LIST] = scaler.transform(
            dataset.loc[:, PAYMENT_FRAUD_MODEL_FEATURE_LIST])
    else:
        dataset.loc[:, PAYMENT_FRAUD_MODEL_FEATURE_LIST] = scale_data(
            dataset.loc[:, PAYMENT_FRAUD_MODEL_FEATURE_LIST])

    return dataset
