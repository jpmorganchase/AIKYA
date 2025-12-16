package com.aikya.orchestrator.repository.agent;

import com.aikya.orchestrator.repository.QueryRepository;
import com.aikya.orchestrator.shared.AikyaAppException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Qualifier("agentQueryRepository")
@Repository
@SuppressWarnings("all")
public class OrchestratorAgentQueryRepository extends QueryRepository {
    @PersistenceContext
    protected EntityManager em;
    public boolean truncateTable(final String table) {
        try {
            // NOSONAR
            em.createNativeQuery("TRUNCATE TABLE "+ table).executeUpdate();
            return true; // Truncate successful
        } catch (Exception e) {
            throw new AikyaAppException(table, e);
        }
    }
    public boolean update(final String sql) {
        try {
            em.createNativeQuery(sql).executeUpdate();
            return true; // Truncate successful
        } catch (Exception e) {
            throw new AikyaAppException(sql, e);
        }
    }
    public boolean executeQuery(final String sql) {
        try {
            return executeQuery(em, sql);
        } catch (Exception e) {
            throw new AikyaAppException(sql, e);
        }
    }
    public <T> List<T> nativeQueryForResultList(final String nativeSql, final Class<T> domain, final Object... params) {
        return nativeQueryForResultList(em, nativeSql, domain, params);
    }
    public <T> T nativeQueryForSigngleResult(final String nativeSql, final Class<T> domain, final Object... params) {
        return nativeQueryForResult(em, nativeSql, domain, params);
    }
    public Long getMaxVersion(final String nativeSql) {
        return getMaxVersion(em, nativeSql);
    }
}
