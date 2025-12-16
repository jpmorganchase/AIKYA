--#query=getPendingServerWorkflow
select wf.id workflow_id, wf.model_id, wd.id workflow_detail_id, wf.current_step, wf.workflow_trace_id, wf.status workflow_status, wf.last_update_date, wd.step, wd.step_desc, wd.label, wd.source, wd.target, wd.status as step_status, wd.created_date
from fedlearn_orchestrator_aggregator.workflow wf, fedlearn_orchestrator_aggregator.workflow_detail wd
where wf.status='Pending' and wd.workflow_id= wf.id
order by wf.last_update_date desc, wf.id asc, wd.step asc