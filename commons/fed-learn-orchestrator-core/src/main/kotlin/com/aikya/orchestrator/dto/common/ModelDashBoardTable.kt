package com.aikya.orchestrator.dto.common

class ModelDashBoardTable (
    val headers:List<Map<String, String>>,
    val rows: List<List<Any?>>
){
    companion object {
        fun empty(): ModelDashBoardTable {
            return ModelDashBoardTable(emptyList(), emptyList())
        }
    }
}