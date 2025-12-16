package com.aikya.orchestrator.repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;

/**
 * the repository can be used for native query service
 *
 * @author bwu
 */
@SuppressWarnings("all")
public class QueryRepository {

    /**
     * @param em
     * @param nativeSql
     * @param params
     * @return
     */
    private <T> Query buildNativeQuery(EntityManager em,
                                       final String nativeSql, Class<T> domain, final Object... params) {
        Query query = em.createNativeQuery(nativeSql, domain);
        if (params != null) {
            int i = 1;
            for (Object param : params) {
                query.setParameter(i, param);
                i++;
            }
        }
        return query;
    }
    public boolean executeQuery(EntityManager em, final String sql) {
        try {
            em.createNativeQuery(sql).executeUpdate();
            return true; // Truncate successful
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * execute native query. select t1.a, t1.b, t2.c, t2,d, t3.z etc from table t1, table t2, table t3
     * where .... queryMapKey is DAO:sqlId in sqlMap.xml
     *
     * @param nativeSql
     * @param domain
     * @param params
     * @return
     */
    public <T> List<T> nativeQueryForResultList(final EntityManager em,
                                                final String nativeSql, final Class<T> domain, final Object... params) {
        try {
            Query query = buildNativeQuery(em, nativeSql, domain, params);
            List<T> result = query.getResultList();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public List nativeQueryForResultList(final EntityManager em, final String nativeSql) {
        try {
            Query query = em.createNativeQuery(nativeSql);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * execute native query. select t1.a, t1.b, t2.c, t2,d, t3.z etc from table t1, table t2, table t3
     * where .... queryMapKey is DAO:sqlId in sqlMap.xml
     *
     * @param nativeSql
     * @param domain
     * @param params
     * @return
     */
    public <T> T nativeQueryForResult(final EntityManager em,
                                                final String nativeSql, final Class<T> domain, final Object... params) {
        try {
            Query query = buildNativeQuery(em, nativeSql, domain, params);
            T result =  (T) query.getSingleResult();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Long getMaxVersion(final EntityManager em, final String nativeSql) {
        Query nativeQuery = em.createNativeQuery(nativeSql);
        Object result = nativeQuery.getSingleResult();
        return result != null ? Long.parseLong(result.toString()) : 0L;
    }
    public Object getRecord(final EntityManager em, final String nativeSql) {
        Query nativeQuery = em.createNativeQuery(nativeSql);
        Object result = nativeQuery.getSingleResult();
        return result;
    }
}
