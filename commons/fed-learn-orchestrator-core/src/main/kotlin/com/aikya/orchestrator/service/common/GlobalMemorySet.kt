package com.aikya.orchestrator.service.common

import java.util.concurrent.ConcurrentHashMap

object GlobalMemorySet {
    private val batchIdSet = ConcurrentHashMap<String, Boolean>()

    fun isBatchIdExist(batchId: String): Boolean {
        return batchIdSet.containsKey(batchId)
    }

    fun addBatchId(batchId: String) {
        batchIdSet[batchId] = true
    }
}
