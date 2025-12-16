package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlCollaborationRunClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@SuppressWarnings("all")
interface FlCollaborationRunClientRepository : JpaRepository<FlCollaborationRunClient, Long> {
    @Query("SELECT c FROM FlCollaborationRunClient c WHERE c.run.id = :runId AND c.client.id = :clientId")
    fun findByRunIdAndClientId(@Param("runId") runId: Long, @Param("clientId") clientId: Long): FlCollaborationRunClient?

    fun findByWorkflowTraceId(workflowTraceId: String): List<FlCollaborationRunClient>

    @Query("SELECT c FROM FlCollaborationRunClient c WHERE c.run.id = :runId AND c.client.id = :clientId AND c.rounds = :round")
    fun findByRunIdAndClientIdAndRound(
        @Param("runId") runId: Long,
        @Param("clientId") clientId: Long,
        @Param("round") round: Int
    ): FlCollaborationRunClient?

    @Query("SELECT COALESCE(MAX(c.rounds), 0) FROM FlCollaborationRunClient c WHERE c.run.id = :runId")
    fun findLatestRoundByRunId(@Param("runId") runId: Long): Int?

    @Query("SELECT COUNT(c) FROM FlCollaborationRunClient c WHERE c.run.id = :runId AND c.rounds = :round")
    fun countByRunIdAndRound(@Param("runId") runId: Long, @Param("round") round: Int): Int

    fun findByClientIdAndWorkflowTraceId(clientId: Long, workflowTraceId: String): List<FlCollaborationRunClient>

    @Query("""
        SELECT c FROM FlCollaborationRunClient c
        WHERE c.run.id = :runId
          AND c.workflowTraceId <> :workflowTraceId
          AND c.client.id <> :clientId
        ORDER BY c.rounds DESC
    """)
    fun findByRunIdAndExcludeWorkflowTraceIdOrderByRounds(
        @Param("runId") runId: Long,
        @Param("workflowTraceId") workflowTraceId: String,
        @Param("clientId") clientId: Long
    ): List<FlCollaborationRunClient>

    @Query("SELECT c FROM FlCollaborationRunClient c JOIN WorkflowEntity w ON c.workflowTraceId = w.workflowTraceId WHERE w.status IN ('Pending', 'Initial') AND c.createdDate <= :someMinutesAgo")
    fun findPendingStalledClients(@Param("someMinutesAgo") someMinutesAgo: Date): List<FlCollaborationRunClient>
}