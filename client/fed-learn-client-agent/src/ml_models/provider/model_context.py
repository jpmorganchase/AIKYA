from src.util.constants import DEFAULT_WORKFLOW_TRACE_ID, ModelClassLoaderTypes


class ModelContext:
    def __init__(self, domain_type, execution_type, model_class_type, file_path, workflow_trace_id=DEFAULT_WORKFLOW_TRACE_ID, batch_id=None):
        self.domain_type = domain_type
        self.execution_type = execution_type
        self.model_class_type = model_class_type
        self.file_path = file_path
        self.workflow_trace_id = workflow_trace_id
        self.batch_id = batch_id
