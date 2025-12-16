package com.aikya.orchestrator.repository.client;

import com.aikya.orchestrator.repository.QueryRepository;
import com.aikya.orchestrator.shared.AikyaAppException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Qualifier("clientQueryRepository")
@Repository
@SuppressWarnings("all")
public class FLClientQueryRepository extends QueryRepository {
    @PersistenceContext
    protected EntityManager em;

    public boolean truncateTable(final String table) {
        try {
            em.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
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

    public Long getCount(final String nativeSql) {
        return getMaxVersion(em, nativeSql);
    }

    public Object getRecord(final String nativeSql) {
        return getRecord(em, nativeSql);
    }

    public List nativeQueryForRawResultList(final String nativeSql) {
        return nativeQueryForResultList(em, nativeSql);
    }
}
