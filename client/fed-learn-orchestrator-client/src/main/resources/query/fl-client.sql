--#query=getPendingClientWorkflow
select wf.id        workflow_id,
       wf.model_id,
       wd.id        workflow_detail_id,
       wf.current_step,
       wf.workflow_trace_id,
       wf.status    workflow_status,
       wf.last_update_date,
       wd.step,
       wd.step_desc,
       wd.label,
       wd.source,
       wd.target,
       wd.status as step_status,
       wd.created_date
from fedlearn_orchestrator_agent.workflow wf,
     fedlearn_orchestrator_agent.workflow_detail wd
where wf.status = 'Pending'
  and wd.workflow_id = wf.id
order by wf.last_update_date desc, wf.id asc, wd.step asc
--#query=getPreviousMlData
SELECT mpd.id, CAST(mpd.result AS DOUBLE) AS result
FROM fedlearn_client.model_predict_data mpd
         JOIN (SELECT d.workflow_trace_id
               FROM fedlearn_client.data_seed d
               WHERE d.file_name = (SELECT ds.file_name
                                    FROM fedlearn_client.data_seed ds
                                    WHERE ds.batch_id = ?1)
                 AND d.batch_id <> ?2
                 AND d.status = 'Complete'
               ORDER BY d.created_date DESC
               LIMIT 1) AS limited_workflow_trace_ids
              ON mpd.workflow_trace_id = limited_workflow_trace_ids.workflow_trace_id
WHERE mpd.result IS NOT NULL
ORDER BY mpd.id
--#query=getCompletedClientWorkflow
select wf.id        workflow_id,
       wf.model_id,
       wd.id        workflow_detail_id,
       wf.current_step,
       wf.workflow_trace_id,
       wf.status    workflow_status,
       wf.last_update_date,
       wd.step,
       wd.step_desc,
       wd.label,
       wd.source,
       wd.target,
       wd.status as step_status,
       wd.created_date
from fedlearn_orchestrator_agent.workflow wf,
     fedlearn_orchestrator_agent.workflow_detail wd
where wf.status = 'Complete'
  and wd.workflow_id = wf.id
order by wf.last_update_date desc, wf.id asc, wd.step asc
--#query=getDomainTable
select distinct(db_table)
from fedlearn_client.model_data_features mdf
where `domain` = ?1
--#query=countMlDatasByBatchSql
SELECT count(DISTINCT (mpd.id)) as count
FROM fedlearn_client.model_predict_data mpd
         JOIN fedlearn_client.&1 t
ON mpd.item_id = t.id
    LEFT JOIN fedlearn_client.model_feedback f ON mpd.id = f.model_data_id
WHERE mpd.batch_id = t.batch_id
  AND mpd.item_id = t.id
  AND mpd.batch_id ='&2'
--#query=getMlDatasByBatchSql
SELECT DISTINCT(mpd.id)     as id,
               mpd.batch_id as batch_id, &1, CASE
    WHEN CAST(mpd.result AS DOUBLE) >= 50 THEN 'Yes'
    ELSE 'No'
    END AS prediction, CAST(mpd.confidence_score AS DOUBLE) AS confidenceScore, f.status
FROM fedlearn_client.model_predict_data mpd
    JOIN fedlearn_client.&2 t
ON mpd.item_id = t.id
    LEFT JOIN fedlearn_client.model_feedback f ON mpd.id = f.model_data_id
WHERE mpd.batch_id = t.batch_id AND mpd.item_id = t.id AND mpd.batch_id ='&3'
order by mpd.id
--#query=getDashboardFraudulentByBatchSql
SELECT dt.id,
       t.batch_id,
       dt.label,
       dt.workflow_trace_id,
       dt.created_date,
       COUNT(DISTINCT t.id) AS total_record_count,
       COALESCE(SUM(CASE
                        WHEN CAST(mpd.result AS DOUBLE) <= 50 THEN 1
                        ELSE 0
           END), 0)         AS anomalous_record_count,
       COALESCE(SUM(CASE
                        WHEN CAST(mpd.result AS DOUBLE) > 50 THEN 1
                        ELSE 0
           END), 0)         AS normal_record_count,
       CASE
           WHEN COUNT(DISTINCT t.id) = 0 THEN 0
           ELSE (COALESCE(SUM(CASE
                                  WHEN CAST(mpd.result AS DOUBLE) >= 50 THEN 1
                                  ELSE 0
               END), 0) / COUNT(DISTINCT t.id) * 100.0)
           END              AS anomalous_percentage,
       COALESCE(SUM(CASE
                        when t.&3 then 1
                        else 0
           END), 0)         as actual_anomalous_count,
       CASE
           WHEN COUNT(DISTINCT t.id) = 0 THEN 0
           ELSE (
               COALESCE(SUM(CASE
                                when t.&3 THEN 1
                                ELSE 0
                   END), 0) / COUNT(DISTINCT t.id) * 100.0
               )
           END              AS actual_anomalous_percentage,
       dt.anomaly_desc
FROM fedlearn_client.&1 t
    JOIN (
        SELECT ds.id, ds.workflow_trace_id, ds.batch_id, ds.created_date, dsm.file_name, dsm.label, dsm.anomaly_desc
        FROM fedlearn_client.data_seed ds
        JOIN fedlearn_client.data_seed_metadata dsm ON ds.file_name = dsm.file_name AND dsm.domain_type = '&2'
    ) AS dt
ON dt.batch_id = t.batch_id
    LEFT JOIN fedlearn_client.model_predict_data mpd ON mpd.item_id = t.id AND mpd.batch_id = t.batch_id
GROUP BY t.batch_id, dt.label, dt.created_date, dt.workflow_trace_id, dt.id, dt.anomaly_desc
ORDER BY dt.created_date desc
--#query=getPreviousMlDatasByBatchSql
SELECT mpd.id,
       CASE
           WHEN CAST(mpd.result AS DOUBLE) >= 50 THEN 'Yes'
           ELSE 'No'
           END                    AS prediction,
       CAST(mpd.result AS DOUBLE) AS confidenceScore,
       f.status
FROM fedlearn_client.model_predict_data mpd
         JOIN fedlearn_client.&1 t
ON mpd.item_id = t.id
    LEFT JOIN fedlearn_client.model_feedback f ON mpd.id = f.model_data_id
WHERE mpd.batch_id = t.batch_id AND mpd.item_id = t.id AND mpd.batch_id ='&2'
order by mpd.id