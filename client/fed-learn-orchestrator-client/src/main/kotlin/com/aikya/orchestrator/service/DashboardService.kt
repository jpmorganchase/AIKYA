package com.aikya.orchestrator.service

import com.aikya.orchestrator.agent.model.ClientRunModeEntity
import com.aikya.orchestrator.conf.WebPropConfig
import com.aikya.orchestrator.dto.DashboardSummaryDTO
import com.aikya.orchestrator.dto.ShapleyValues
import com.aikya.orchestrator.dto.common.ClientRunModel
import com.aikya.orchestrator.dto.common.DashboardConfig
import com.aikya.orchestrator.dto.common.GridColumnDef
import com.aikya.orchestrator.dto.common.makeChildColumnsRecursively
import com.aikya.orchestrator.repository.client.FLClientQueryRepository
import com.aikya.orchestrator.repository.client.TableColumnInfo
import com.aikya.orchestrator.service.common.QueryLoaderService
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_3
import com.aikya.orchestrator.utils.AppConstants.RunModeOptionsEnum
import com.aikya.orchestrator.utils.AppUtils
import com.aikya.orchestrator.utils.AppUtils.convertQueryResultToKeyValueList
import com.aikya.orchestrator.utils.AppUtils.dateToYYYYMMDDHHMMSS
import com.aikya.orchestrator.utils.AppUtils.display
import com.aikya.orchestrator.utils.FileUtils
import com.aikya.orchestrator.utils.JsonUtils
import jakarta.annotation.PostConstruct
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * Service responsible for fetching, processing, and formatting all data required for the client's UI dashboards. 📊
 *
 * This class acts as a central hub for dashboard-related functionality, providing a clean interface
 * for the frontend to retrieve complex data sets. Its responsibilities include:
 * - Loading dashboard grid configurations from JSON files.
 * - Fetching main dashboard data, summarizing training batches and their outcomes.
 * - Providing detailed, paginated prediction data for a specific batch, including comparisons with previous model versions.
 * - Managing the run mode (AUTO/MANUAL) for workflows and weight updates.
 * - Supplying data for model explainability charts (SHAP values).
 *
 * @param webPropConfig Application-wide web properties and configurations.
 * @param queryLoaderService Service for loading predefined SQL queries.
 * @param dataSeedsService Service for managing data batch (seed) metadata.
 * @param clientFacadeService Facade service for high-level client operations.
 * @param workflowModelLogsService Service for managing run modes and model logs.
 * @param modelService Service for accessing model prediction and record data.
 * @param orchestrationClientCallService Service for making outbound calls to the orchestrator.
 */
@Service
class DashboardService(
    private val webPropConfig: WebPropConfig,
    private val queryLoaderService: QueryLoaderService,
    private val dataSeedsService: DataSeedsService,
    private val clientFacadeService: ClientFacadeService,
    private val workflowModelLogsService: WorkflowModelLogsService,
    private val modelService: ModelService,
    private val orchestrationClientCallService: OrchestrationClientCallService
) {

    private val LABEL_TOTAL = "Total"
    private val LABEL_CURRENT_ANOMALOUS = "Current Anomalous"
    private val LABEL_CURRENT_ANOMALY_PCT = "Anomaly (%)"
    private val LABEL_PREVIOUS_ANOMALOUS = "Previous Anomalous"
    private val LABEL_PREVIOUS_ANOMALY_PCT = "Previous Anomaly (%)"

    @Autowired
    @Qualifier("clientQueryRepository")
    private lateinit var flClientQueryRepository: FLClientQueryRepository
    private val logger: Logger = LoggerFactory.getLogger(ClientFacadeService::class.java)

    @Autowired
    lateinit var resourceResolver: ResourcePatternResolver

    @Value("\${app.dashboard-conf:}")
    private lateinit var dashboardConfPath: String

    @Value("\${app.dashboard-detail-conf:}")
    private lateinit var dashboardDetailConfPath: String

    private var dashboardColumns = mutableListOf<GridColumnDef>()
    private var dashboardDetailColumns = mutableListOf<GridColumnDef>()
    /**
     * Initializes the service by loading the JSON configurations for the dashboard grids
     * from either a specified file path or default classpath resources.
     */
    @PostConstruct
    fun init() {
        try {
            dashboardColumns = loadConfig(
                confPath = dashboardConfPath,
                defaultResourcePath = "classpath:dashboard/dashboard.json",
                parseFunction = ::parseColumnsFromJson
            )
            display(logger, "Loaded ${dashboardColumns.size} dashboard configuration")

            dashboardDetailColumns = loadConfig(
                confPath = dashboardDetailConfPath,
                defaultResourcePath = "classpath:dashboard/dashboard-detail.json",
                parseFunction = ::parseColumnsFromJson
            )
            display(logger, "Loaded ${dashboardDetailColumns.size} dashboard detail configuration")
        } catch (e: Exception) {
            throw UnsupportedOperationException(
                "Error occurs when initializing load smart contract config: ", e
            )
        }
    }

    private fun loadConfig(
        confPath: String, defaultResourcePath: String, parseFunction: (String) -> List<GridColumnDef>
    ): MutableList<GridColumnDef> {
        val columns = mutableListOf<GridColumnDef>()
        if (confPath.isNotBlank()) {
            logger.info("Loading configuration from external path: $confPath")
            val rawFileStr = FileUtils.readFromFilePath(confPath)
            columns.addAll(parseFunction(rawFileStr))
        } else {
            logger.info("Loading default configuration from: $defaultResourcePath")
            val resources: Array<Resource> = resourceResolver.getResources(defaultResourcePath)
            for (resource in resources) {
                val rawFileStr = FileUtils.readFileAsString(resource.inputStream)
                columns.addAll(parseFunction(rawFileStr))
            }
        }
        return columns
    }

    private fun parseColumnsFromJson(rawFileStr: String): List<GridColumnDef> {
        try {
            return JsonUtils.readJsonAsList(rawFileStr, GridColumnDef::class.java)
        } catch (e: Exception) {
            logger.error("Failed to load dashboard configuration", e)
            throw UnsupportedOperationException("Error occurs while loading dashboard configuration: ", e)
        }
    }
    /**
     * Fetches the raw feature data for a single predicted item.
     *
     * @param domain The learning domain.
     * @param id The ID of the record in the `model_predict_data` table.
     * @return A list of key-value pairs representing the item's feature data, or an empty list if not found.
     */
    fun getItemData(domain: String, id: Long): List<Map<String, Any?>> {
        val modelPredictDataEntity = modelService.findModelPredictionById(id)
        if (modelPredictDataEntity.isPresent) {
            val modelPredictData = modelPredictDataEntity.get()
            val domainTableId = modelPredictData.itemId
            val domainTable = queryLoaderService.getQuery("getDomainTable")
            val tableName = flClientQueryRepository.nativeQueryForSigngleResult(
                domainTable, String::class.java, domain
            )
            val columns = getTableColumns(tableName)
            val columnNames = columns.joinToString(", ") { it.columnName }
            val getDomainDataByIdSql = "SELECT $columnNames FROM fedlearn_client.&1 mdf WHERE id=&2"
            val formattedSql = getDomainDataByIdSql.replace("&1", tableName).replace("&2", domainTableId.toString())
            val result = flClientQueryRepository.getRecord(formattedSql)
            return convertQueryResultToKeyValueList(columns, result)
        }
        return emptyList()
    }
    /**
     * Retrieves column names and data types for a given table from the database schema.
     *
     * @param tableName The name of the table to inspect.
     * @return A list of `TableColumnInfo` objects.
     */
    fun getTableColumns(tableName: String?): List<TableColumnInfo> {
        val nativeSql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ?1"
        val columns: List<TableColumnInfo> = flClientQueryRepository.nativeQueryForResultList(
            nativeSql, TableColumnInfo::class.java, tableName
        )
        return columns
    }
    /**
     * Retrieves the domain-specific dashboard configuration object.
     *
     * @param domain The domain name.
     * @return The `DashboardConfig` for the domain, or `null` if not found.
     */
    fun getDashboardConfigByDomain(domain: String): DashboardConfig? {
        return webPropConfig.dashboard.find { it.domain == domain }
    }
    /**
     * Gets all prediction data for a given batch, formatted for a simple data table.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @return A map containing headers, rows, summary, and paging info.
     */
    @Transactional(readOnly = true)
    fun getItemPredictDataByBatch(domain: String, batchId: String): Map<String, Any> {
        return getPagingItemPredictDataByBatch(domain, batchId, 50000, 0)
    }
    /**
     * Gets all prediction data for a given batch, formatted for an advanced data grid.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @return A map containing grouped headers, data, summary, and paging info.
     */
    @Transactional(readOnly = true)
    fun getItemPredictGridByBatch(domain: String, batchId: String): Map<String, Any> {
        return getPagingItemPredictGridByBatch(domain, batchId, 50000, 0)
    }
    /**
     * Fetches paginated prediction results for a given batch, formatted for an advanced data grid.
     * This method dynamically generates grid headers, fetches both current and previous predictions
     * for comparison, calculates summary statistics, and provides comprehensive paging information.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @param limit The maximum number of records to return.
     * @param offset The starting offset for pagination.
     * @return A map containing `headers`, `data`, `summary`, and `paging` information.
     */
    @Transactional(readOnly = true)
    fun getPagingItemPredictGridByBatch(domain: String, batchId: String, limit: Int, offset: Int): Map<String, Any> {
        val dashboardConfig = getDashboardConfigByDomain(domain)!!
        val table = dashboardConfig.mapping.table
        val dbColumns = dashboardConfig.mapping.dbColumns
        val webLabels = dashboardConfig.mapping.webLabels

        // Get the SQL template for counting total records
        val countMlDatasByBatchSqlTemplate = queryLoaderService.getQuery("countMlDatasByBatchSql")
        val countMlDatasByBatchSql = countMlDatasByBatchSqlTemplate.replace("&1", table).replace("&2", batchId)

        // Execute the count query to get the total number of records
        val totalRecords = flClientQueryRepository.getCount(countMlDatasByBatchSql)

        val getMlDatasByBatchSqlTemplate = queryLoaderService.getQuery("getMlDatasByBatchSql")
        val columnsWithPrefix = dbColumns.joinToString(", ") { "t.$it" }
        val sqlWithColumns = getMlDatasByBatchSqlTemplate.replace("&1", columnsWithPrefix)
        val sqlWithTable = sqlWithColumns.replace("&2", table)
        val getMlDatasByBatchSql = sqlWithTable.replace("&3", batchId)
        val paginatedSql = "$getMlDatasByBatchSql LIMIT $limit OFFSET $offset"

        // Fetch the current data
        val dataList = flClientQueryRepository.nativeQueryForRawResultList(paginatedSql).toList()
        val dynamicColumns = dbColumns.mapIndexed { index, columnName ->
            val headerName = webLabels.getOrNull(index) ?: columnName
            val columnDef = GridColumnDef()
            columnDef.headerName = headerName
            columnDef.field = columnName
            columnDef.type = "rightAligned"
            columnDef.width = 120
            // Set other properties if needed
            columnDef
        }
        val dynamicHeaders = dynamicColumns.map { column ->
            mapOf(
                "headerName" to column.headerName,
                "field" to column.field,
                "type" to column.type,
                "width" to column.width
                // Add other properties if needed
            )
        }
        // Generate headers
        val headers = dashboardDetailColumns.map { column ->
            // Simply convert each ColumnDefinition to a map representation (like JSON)
            mapOf(
                "headerName" to column.headerName,
                "field" to column.field,
                "type" to column.type,
                "filter" to column.filter,
                "lockPinned" to column.lockPinned,
                "pinned" to column.pinned,
                "filterParams" to column.filterParams?.let {
                    mapOf("buttons" to it.buttons)
                },
                "headerTooltip" to column.headerTooltip,
                "editable" to column.editable,
                "cellClass" to column.cellClass,
                "hide" to column.hide,
                "width" to column.width,
                "children" to if (column.children != null && column.children!!.isNotEmpty()) column.children else null,
            ).filterValues { it != null } // Remove any null values to keep the structure clean
        }.toMutableList()
        val itemIdIndex = headers.indexOfFirst { it["field"] == "batchId" }
        val insertionIndex = if (itemIdIndex != -1) itemIdIndex + 1 else headers.size
        // Generate headers
        headers.addAll(insertionIndex, dynamicHeaders)

        // Retrieve previous batch data for 'previousPrediction'
        val previousBatchData = getPreviousBatchData(batchId, table, limit, offset).toList()
        val previousPredictionMap = previousBatchData[1]

        // Modify 'dataList' by inserting 'previousPrediction' after 'prediction'
        val predictionIndex = headers.indexOfFirst { it["field"] == "prediction" }
        val modifiedDataList = dataList.mapIndexed { rowIndex, row ->
            val rowWithPrediction = when (row) {
                is Array<*> -> row.toMutableList()
                is List<*> -> row.toMutableList()
                is Collection<*> -> row.toMutableList()
                else -> mutableListOf(row) // Fallback for single, non-collection items
            }
            val previousPrediction = previousPredictionMap[rowIndex]?.toString() ?: "-"
            rowWithPrediction.add(predictionIndex + 1, previousPrediction) // Insert after 'prediction'
            rowWithPrediction
        }

        val rows = modifiedDataList.map { row ->
            val rowMap = mutableMapOf<String, Any?>()

            row.forEachIndexed { i, value ->
                val fieldName = headers[i]["field"] ?: "field$i"
                val formattedValue = when (value) {
                    null -> ""  // Handle null values appropriately
                    is Long, is Int -> value
                    is Double -> BigDecimal(value).setScale(2, RoundingMode.FLOOR).toPlainString()
                    else -> value.toString()
                }
                rowMap[fieldName.toString()] = formattedValue
            }
            rowMap
        }
        // Step 6: Create summary and paging information
        val fraudCount = rows.count { it["prediction"] == "Yes" }
        val fraudPercentage = if (totalRecords > 0) {
            BigDecimal(fraudCount.toDouble() / totalRecords * 100).setScale(2, RoundingMode.FLOOR)
        } else {
            BigDecimal.ZERO
        }

        val previousFraudPercentage = if (totalRecords > 0) {
            BigDecimal(previousPredictionMap.values.count { it == "Yes" }.toDouble() / totalRecords * 100).setScale(
                2, RoundingMode.FLOOR
            )
        } else {
            BigDecimal.ZERO
        }

        val currentPage = (offset / limit) + 1
        val totalPages = if ((totalRecords % limit).toInt() == 0) totalRecords / limit else (totalRecords / limit) + 1

        // Create the summary as a list containing the percentage value
        val summary = listOf(
            mapOf("field" to "total", "label" to LABEL_TOTAL, "value" to totalRecords.toString()),
            mapOf("field" to "fraudCount", "label" to LABEL_CURRENT_ANOMALOUS, "value" to fraudCount.toString()),
            mapOf("field" to "fraudPct", "label" to LABEL_CURRENT_ANOMALY_PCT, "value" to fraudPercentage.toPlainString() + '%'),
            mapOf(
                "field" to "previousFraudCount",
                "label" to LABEL_PREVIOUS_ANOMALOUS,
                "value" to previousPredictionMap.values.count { it == "Yes" }.toString()
            ),
            mapOf(
                "field" to "previousFraudPct",
                "label" to LABEL_PREVIOUS_ANOMALY_PCT,
                "value" to previousFraudPercentage.toPlainString() + '%'
            )
        )
        // Group headers dynamically
        val dataSummaryHeaders = headers.take(predictionIndex) + dynamicHeaders
        val insightsModelResultsHeaders = headers.filter {
            it["field"] in listOf("prediction", "previousPrediction", "confidenceScore", "status")
        }

        // Create grouped headers
        val groupedHeaders = listOf(
            mapOf(
                "headerName" to "Data Summary",
                "children" to dataSummaryHeaders,
                "cellClass" to "groupheaderLeft"
            ),
            mapOf(
                "headerName" to "Insights/Model Results",
                "children" to insightsModelResultsHeaders
            )
        )
        // Create the final JSON output
        val output = mapOf(
            "headers" to groupedHeaders, "data" to rows, "summary" to summary, "paging" to mapOf(
                "totalRecords" to totalRecords,
                "totalPages" to totalPages,
                "currentPage" to currentPage,
                "pageSize" to limit,
                "previousPage" to if (currentPage > 1) currentPage - 1 else null,
                "nextPage" to if (currentPage < totalPages) currentPage + 1 else null
            )
        )
        return output
    }
    /**
     * Provides a filtered view of the prediction data, showing *only* the records where the
     * current model's prediction differs from the previous one.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @param limit The maximum number of records to return.
     * @param offset The starting offset for pagination.
     * @return A map containing headers, the filtered data, summary, and paging info.
     */
    @Transactional(readOnly = true)
    fun getDiffItemPredictDataByBatch(domain: String, batchId: String, limit: Int, offset: Int): Map<String, Any> {
        val result = getPagingItemPredictGridByBatch(domain, batchId, limit, offset)
        // Extract the headers and rows from the result
        val headers = (result["headers"] as? List<*>)?.filterIsInstance<Map<String, String>>() ?: emptyList()

        val rows = (result["data"] as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()

        // Find the indices for 'prediction' and 'previousPrediction' in the headers
        val predictionField = headers.find { it["field"] == "prediction" }?.get("field") ?: "prediction"
        val previousPredictionField =
            headers.find { it["field"] == "previousPrediction" }?.get("field") ?: "previousPrediction"

        // Filter the rows where the current prediction and previous prediction differ
        val diffRows = rows.filter { row ->
            val currentPrediction = row[predictionField]?.toString() ?: ""
            val previousPrediction = row[previousPredictionField]?.toString() ?: ""
            currentPrediction != previousPrediction
        }

        // Fraud statistics calculations
        val totalAfterComparison = diffRows.size
        val fraudCount = diffRows.count { row -> row[predictionField]?.toString() == "Yes" }
        val previousFraudCount = diffRows.count { row -> row[previousPredictionField]?.toString() == "Yes" }

        val fraudPct = if (totalAfterComparison > 0) {
            BigDecimal(fraudCount.toDouble() / totalAfterComparison * 100).setScale(2, RoundingMode.FLOOR)
                .toPlainString()
        } else {
            "0.00"
        }

        val previousFraudPct = if (totalAfterComparison > 0) {
            BigDecimal(previousFraudCount.toDouble() / totalAfterComparison * 100).setScale(2, RoundingMode.FLOOR)
                .toPlainString()
        } else {
            "0.00"
        }
        // Summary
        val summary = listOf(
            mapOf("field" to "total", "label" to LABEL_TOTAL, "value" to totalAfterComparison.toString()),
            mapOf("field" to "fraudCount", "label" to LABEL_CURRENT_ANOMALOUS, "value" to fraudCount.toString()),
            mapOf("field" to "fraudPct", "label" to LABEL_CURRENT_ANOMALY_PCT, "value" to "$fraudPct%"),
            mapOf(
                "field" to "previousFraudCount",
                "label" to LABEL_PREVIOUS_ANOMALOUS,
                "value" to previousFraudCount.toString()
            ),
            mapOf("field" to "previousFraudPct", "label" to "Previous Anomaly (%)", "value" to "$previousFraudPct%")
        )
        // Paging calculations
        val totalRecords = totalAfterComparison
        val totalPages = if (totalRecords % limit == 0) totalRecords / limit else totalRecords / limit + 1
        val currentPage = (offset / limit) + 1
        val previousPage = if (currentPage > 1) currentPage - 1 else null
        val nextPage = if (currentPage < totalPages) currentPage + 1 else null

        val paging = mapOf(
            "totalRecords" to totalRecords,
            "totalPages" to totalPages,
            "currentPage" to currentPage,
            "pageSize" to limit,
            "previousPage" to previousPage,
            "nextPage" to nextPage
        )

        // Create the output map
        val output: Map<String, Any> = mapOf(
            "headers" to headers, "data" to diffRows, "summary" to summary, "paging" to paging
        )

        return output
    }
    /**
     * Fetches paginated prediction results for a given batch, formatted for a simple data table.
     * This version uses a simpler, non-grouped header structure.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @param limit The maximum number of records to return.
     * @param offset The starting offset for pagination.
     * @return A map containing `headers`, `rows`, `summary`, and `paging` information.
     */
    @Transactional(readOnly = true)
    fun getPagingItemPredictDataByBatch(domain: String, batchId: String, limit: Int, offset: Int): Map<String, Any> {
        val dashboardConfig = getDashboardConfigByDomain(domain)!!
        val table = dashboardConfig.mapping.table
        val dbColumns = dashboardConfig.mapping.dbColumns
        val webLabels = dashboardConfig.mapping.webLabels

        // Get the SQL template for counting total records
        val countMlDatasByBatchSqlTemplate = queryLoaderService.getQuery("countMlDatasByBatchSql")
        val countMlDatasByBatchSql = countMlDatasByBatchSqlTemplate.replace("&1", table).replace("&2", batchId)

        // Execute the count query to get the total number of records
        val totalRecords = flClientQueryRepository.getCount(countMlDatasByBatchSql)

        val getMlDatasByBatchSqlTemplate = queryLoaderService.getQuery("getMlDatasByBatchSql")
        val columnsWithPrefix = dbColumns.joinToString(", ") { "t.$it" }
        val sqlWithColumns = getMlDatasByBatchSqlTemplate.replace("&1", columnsWithPrefix)
        val sqlWithTable = sqlWithColumns.replace("&2", table)
        val getMlDatasByBatchSql = sqlWithTable.replace("&3", batchId)
        val paginatedSql = "$getMlDatasByBatchSql LIMIT $limit OFFSET $offset"

        // Fetch the current data
        val dataList = flClientQueryRepository.nativeQueryForRawResultList(paginatedSql).toList()

        // Generate headers
        val headers = mutableListOf<Map<String, String>>()
        headers.add(mapOf("field" to "id", "label" to "Id", "tooltip" to "Id"))
        headers.add(mapOf("field" to "batchId", "label" to "Batch Id", "tooltip" to "Batch Id"))

        dbColumns.forEachIndexed { index, column ->
            headers.add(mapOf("field" to column, "label" to webLabels[index], "tooltip" to webLabels[index]))
        }

        headers.add(
            mapOf(
                "field" to "prediction", "label" to "Anomalous", "tooltip" to "Model predicted this record as anomalous"
            )
        )
        headers.add(
            mapOf(
                "field" to "previousPrediction",
                "label" to "Previous Anomalous",
                "tooltip" to "Previous Model prediction for the same record"
            )
        )
        headers.add(
            mapOf(
                "field" to "confidenceScore", "label" to "Model Confidence Score", "tooltip" to "Degree of Confidence"
            )
        )
        headers.add(
            mapOf(
                "field" to "status",
                "label" to "User Feedback",
                "tooltip" to "Allows User to override model predicted decision. This override will be used in retraining the model"
            )
        )
        // Retrieve previous data using the new function
        val previousBatchData = getPreviousBatchData(batchId, table, limit, offset).toList()
        val previousPredictionMap = previousBatchData[1]

        var fraudCount = 0

        // Map current data and include previous confidenceScore
        val rows = dataList.mapIndexed { index, row ->
            // Safely convert 'row' to a List without an unchecked cast
            val values: List<Any?> = when (row) {
                is Array<*> -> row.toList()
                is List<*> -> row // No conversion needed if it's already a List
                is Collection<*> -> row.toList()
                else -> listOf(row) // Fallback for single, non-collection items
            }

            // Map over the guaranteed 'List' to format values
            val rowList = values.map { value ->
                when (value) {
                    null -> "" // Handle null values appropriately
                    is Long, is Int -> value
                    is Double -> BigDecimal(value).setScale(2, RoundingMode.FLOOR).toPlainString()
                    is String -> value
                    is java.sql.Date -> dateToYYYYMMDDHHMMSS(value)  // Format the date as needed
                    else -> value.toString()
                }
            }.toMutableList() // Convert to MutableList to allow modification

            // The rest of your logic remains the same
            val predictionIndex = headers.indexOfFirst { it["field"] == "prediction" }
            val previousPrediction = previousPredictionMap[index] ?: "-"
            rowList.add(predictionIndex + 1, previousPrediction)
            val prediction = rowList[predictionIndex]

            // Check if the prediction is "Yes" and increment the count
            if (prediction == "Yes") {
                fraudCount++
            }

            rowList
        }

        val fraudPercentage = if (totalRecords > 0) {
            BigDecimal(fraudCount.toDouble() / totalRecords * 100).setScale(2, RoundingMode.FLOOR)
        } else {
            BigDecimal.ZERO
        }

        val previousFraudPercentage = if (totalRecords > 0) {
            BigDecimal(previousPredictionMap.values.count { it == "Yes" }.toDouble() / totalRecords * 100).setScale(
                2, RoundingMode.FLOOR
            )
        } else {
            BigDecimal.ZERO
        }

        val currentPage = (offset / limit) + 1
        val totalPages = if ((totalRecords % limit).toInt() == 0) totalRecords / limit else (totalRecords / limit) + 1


        // Create the summary as a list containing the percentage value
        val summary = listOf(
            mapOf("field" to "total", "label" to LABEL_TOTAL, "value" to totalRecords.toString()),
            mapOf("field" to "fraudCount", "label" to LABEL_CURRENT_ANOMALOUS, "value" to fraudCount.toString()),
            mapOf("field" to "fraudPct", "label" to LABEL_CURRENT_ANOMALY_PCT, "value" to fraudPercentage.toPlainString() + '%'),
            mapOf(
                "field" to "previousFraudCount",
                "label" to LABEL_PREVIOUS_ANOMALOUS,
                "value" to previousPredictionMap.values.count { it == "Yes" }.toString()
            ),
            mapOf(
                "field" to "previousFraudPct",
                "label" to LABEL_PREVIOUS_ANOMALY_PCT,
                "value" to previousFraudPercentage.toPlainString() + '%'
            )
        )

        // Create the final JSON output
        val output = mapOf(
            "headers" to headers, "rows" to rows, "summary" to summary, "paging" to mapOf(
                "totalRecords" to totalRecords,
                "totalPages" to totalPages,
                "currentPage" to currentPage,
                "pageSize" to limit,
                "previousPage" to if (currentPage > 1) currentPage - 1 else null,
                "nextPage" to if (currentPage < totalPages) currentPage + 1 else null
            )
        )
        return output
    }

    /**
     * Helper method to find the immediately preceding data batch and retrieve the model's
     * predictions and confidence scores for its data.
     *
     * @param batchId The current batch ID, used as a reference point.
     * @param table The domain-specific data table name.
     * @param limit The page size for fetching data.
     * @param offset The page offset for fetching data.
     * @return A `Pair` containing two maps: one for previous confidence scores and one for previous predictions, indexed by row number.
     */
    fun getPreviousBatchData(
        batchId: String, table: String, limit: Int, offset: Int
    ): Pair<MutableMap<Int, String>, MutableMap<Int, String>> {
        // Initialize maps to store previous confidence and prediction data
        val previousConfidenceMap = mutableMapOf<Int, String>()
        val previousPredictionMap = mutableMapOf<Int, String>()

        // Find the previous data seed
        val previousDataSeed = dataSeedsService.findPreviousDataSeed(batchId)
        var previousFraudCount = 0

        if (previousDataSeed != null) {
            val prevBatchId = previousDataSeed.batchId!!
            val getPreviousMlDatasByBatchSqlTemplate = queryLoaderService.getQuery("getPreviousMlDatasByBatchSql")

            // Replace placeholders with actual table and previous batch ID
            val previousSqlWithTable = getPreviousMlDatasByBatchSqlTemplate.replace("&1", table)
            val getPreviousMlDatasByBatchSql = previousSqlWithTable.replace("&2", prevBatchId.toString())

            // Apply pagination to the query
            val previousPaginatedSql = "$getPreviousMlDatasByBatchSql LIMIT $limit OFFSET $offset"

            // Execute the query and retrieve previous data
            val previousDataList = flClientQueryRepository.nativeQueryForRawResultList(previousPaginatedSql).toList()

            // Process the result set
            previousDataList.forEachIndexed { index, row ->
                // Safely convert 'row' to a List without an unchecked cast
                val rowAsList: List<Any?> = when (row) {
                    is Array<*> -> row.toList()
                    is List<*> -> row
                    else -> listOf(row) // Fallback for non-collection items
                }

                // Safely access elements by index using getOrNull()
                val confidenceScore = rowAsList.getOrNull(2)?.toString() ?: ""
                val prediction = rowAsList.getOrNull(1)?.toString() ?: ""

                previousConfidenceMap[index] = confidenceScore
                previousPredictionMap[index] = prediction

                // Count fraud predictions
                if (prediction == "Yes") {
                    previousFraudCount++
                }
            }
        }
        // Return both maps
        return Pair(previousConfidenceMap, previousPredictionMap)
    }
    /**
     * Gets a filtered view of the prediction data, showing *only* the records where the
     * current model's prediction differs from the previous one.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @return A map containing headers, the filtered data, summary, and paging info.
     */
    @Transactional(readOnly = true)
    fun getDiffItemPredictDataByBatch(domain: String, batchId: String): Map<String, Any> {
        return getDiffItemPredictDataByBatch(domain, batchId, 50000, 0)
    }
    /**
     * Filters the main dashboard view by a given date range.
     *
     * @param domain The learning domain.
     * @param start The start date string (e.g., "YYYY-MM-DD").
     * @param end The end date string (e.g., "YYYY-MM-DD").
     * @return A map containing the original headers and the date-filtered data.
     */
    fun searchDashBoard(domain: String, start: String, end: String): Map<String, Any?> {
        // Call getModelDashboardGridData to retrieve the full data
        val result = getModelDashboardGridData(domain)
        val startDateTimestamp = AppUtils.parseStartOfDay(start)
        val endDateTimestamp = AppUtils.parseEndOfDay(end)
        // Extract the data from the result
        val rawData = result["data"]
        val data = if (rawData is List<*>) {
            rawData.mapNotNull { item ->
                @Suppress("UNCHECKED_CAST")
                val map = item as? Map<String, Any?>
                map?.takeIf {
                    val createdDate = it["createdDate"] as? Long ?: 0L
                    createdDate in startDateTimestamp..endDateTimestamp
                }
            }
        } else {
            emptyList()
        }

        // Return the filtered result with the original headers
        return mapOf(
            "headers" to result["headers"],
            "data" to data
        )
    }
    /**
     * Fetches the data for the main dashboard and formats it for a data grid,
     * including dynamically generated headers from the loaded configuration.
     *
     * @param domain The learning domain.
     * @return A map containing `headers` and `data` for the grid.
     */
    fun getModelDashboardGridData(domain: String): Map<String, Any?> {
        val dashboardSummaryList = getModelDashBoard(domain)
        // Create headers dynamically based on dashboardColumns
        val headers = dashboardColumns.map { column ->
            val childCols = makeChildColumnsRecursively(column.children);
            // Simply convert each ColumnDefinition to a map representation (like JSON)
            val representation = mapOf(
                "headerName" to column.headerName,
                "field" to column.field,
                "type" to column.type,
                "filter" to column.filter,
                "lockPinned" to column.lockPinned,
                "pinned" to column.pinned,
                "filterParams" to column.filterParams?.let {
                    mapOf("buttons" to it.buttons)
                },
                "editable" to column.editable,
                "cellClass" to column.cellClass,
                "hide" to column.hide,
                "flex" to column.flex,
                "headerClass" to column.headerClass
            ).filterValues { it != null }.toMutableMap() // Remove any null values to keep the structure clean
            if (childCols.isNotEmpty()) {
                representation["children"] = childCols
            }
            representation;
        }.toList()

        // Convert each DashboardSummary to a map and collect them into a list
        val data = dashboardSummaryList.map { summary ->
            // Construct the map for this summary entry
            mapOf(
                "name" to summary.name,
                "anomalyDesc" to summary.anomalyDesc,
                "modelVersion" to summary.modelVersion,
                "totalRecordCount" to (summary.totalRecordCount ?: 0),
                "anomalousRecordCount" to String.format(
                    "%s (%s)",
                    summary.anomalousRecordCount ?: 0,
                    summary.anomalousPercentage
                ),
                // Add % only if anomalousPercentage is not empty, and convert "0.00%" to "0%"
                // "anomalousPercentage" to summary.anomalousPercentage,
                "actualAnomalousRecordCount" to String.format(
                    "%s (%s)",
                    summary.actualAnomalousRecordCount ?: 0,
                    summary.actualAnomalousPercentage
                ),
                // Add % only if anomalousPercentage is not empty, and convert "0.00%" to "0%"
                // "actualAnomalousPercentage" to summary.actualAnomalousPercentage,
                "createdDate" to summary.createdDate?.time,
                "action" to summary.batch_id
            )
        }

        // Return the result as a map containing headers and data
        return mapOf(
            "headers" to headers, "data" to data
        )
    }
    /**
     * Fetches the summary data for the main dashboard, querying and joining across
     * several tables to get batch information, record counts, and anomaly percentages.
     *
     * @param domain The learning domain.
     * @return A sorted list of `DashboardSummaryDTO` objects.
     */
    fun getModelDashBoard(domain: String): List<DashboardSummaryDTO> {
        val getMlDashboardByBatchSqlTemplate = queryLoaderService.getQuery("getDashboardFraudulentByBatchSql");
        val dashboardConfig = getDashboardConfigByDomain(domain)!!
        val table = dashboardConfig.mapping.table;
        val flagDbColumn = dashboardConfig.mapping.flagDbColumn;
        val getMlDashboardByBatchSql =
            getMlDashboardByBatchSqlTemplate.replace("&1", table).replace("&2", domain).replace("&3", flagDbColumn);
        val dashBoardResults = flClientQueryRepository.nativeQueryForRawResultList(getMlDashboardByBatchSql).toList();
        val workflowTraceIds = dashBoardResults.mapNotNull {
            if (it is Array<*>) it.getOrNull(3)?.toString() else null
        }
        val workflows = clientFacadeService.findWorkflowsByTraceIds(workflowTraceIds);
        return dashBoardResults.mapNotNull { resultRow ->
            if (resultRow is Array<*>) {
                val traceId = resultRow.getOrNull(3)?.toString()

                val currentWorkflow = workflows.find { it.workflowTraceId == traceId }
                val modelVersion = currentWorkflow?.modelVersion?.let { "v-$it" } ?: "v-0"

                DashboardSummaryDTO().apply {
                    batch_id = resultRow.getOrNull(1)?.toString()
                    name = resultRow.getOrNull(2)?.toString() ?: ""
                    this.modelVersion = modelVersion
                    workflowTraceId = traceId
                    this.createdDate = resultRow.getOrNull(4) as? Date
                    totalRecordCount = (resultRow.getOrNull(5) as? Number)?.toInt()

                    anomalousRecordCount = (resultRow.getOrNull(7) as? Number)?.toInt()
                    anomalousPercentage = (resultRow.getOrNull(8) as? Number)?.toDouble()?.let { percentage ->
                        if (percentage.compareTo(0.0) == 0) "0%" else "%.2f".format(percentage) + "%"
                    } ?: "-"

                    actualAnomalousRecordCount = (resultRow.getOrNull(9) as? Number)?.toInt()
                    actualAnomalousPercentage = (resultRow.getOrNull(10) as? Number)?.toDouble()?.let { percentage ->
                        if (percentage.compareTo(0.0) == 0) "0%" else "%.2f".format(percentage) + "%"
                    } ?: "-"
                    anomalyDesc = resultRow.getOrNull(11) as? String
                }
            } else {
                logger.warn("Unexpected result row type: ${resultRow?.javaClass?.name}")
                null
            }
        }.sortedBy { it.createdDate }
    }

    private fun createClientRunModel(clientRunModeEntity: ClientRunModeEntity): ClientRunModel {
        return ClientRunModel().apply {
            id = clientRunModeEntity.id
            mode = clientRunModeEntity.mode
            domain = clientRunModeEntity.domain
            name = clientRunModeEntity.name
            // Populate the modes list with values from RunModeOptionsEnum
            modes = RunModeOptionsEnum.values().map { it.mode }.toMutableList()
        }
    }
    /**
     * Retrieves the current run mode (e.g., AUTO, MANUAL) for the workflow process.
     *
     * @param domainType The learning domain.
     * @return A `ClientRunModel` object representing the current mode.
     */
    fun getWorkflowRunModel(domainType: String): ClientRunModel {
        val clientRunModeEntity = workflowModelLogsService.getWorkflowRunModel(domainType)
        return createClientRunModel(clientRunModeEntity)
    }
    /**
     * Retrieves the current run mode (e.g., AUTO, MANUAL) for the model weight update process.
     *
     * @param domainType The learning domain.
     * @return A `ClientRunModel` object representing the current mode.
     */
    fun getWeightRunModel(domainType: String): ClientRunModel {
        val clientRunModeEntity = workflowModelLogsService.getWeightRunModel(domainType)
        return createClientRunModel(clientRunModeEntity)
    }
    /**
     * Toggles the workflow run mode between AUTO and MANUAL. If switched to AUTO,
     * it checks for pending workflows and may trigger a task to share the local model.
     *
     * @param modeId The ID of the run mode record to toggle.
     * @return The updated `ClientRunModel`.
     */
    @Transactional
    fun toggleWorkflowRunModel(modeId: Long): ClientRunModel {
        val clientRunModeEntity = toggleModeAndSave(modeId)
        if (clientRunModeEntity.mode == RunModeOptionsEnum.AUTO.mode) {
            val domain = clientRunModeEntity.domain
            val pendingWorkflows = clientFacadeService.getPendingClientWorkflow(domain)
            if (pendingWorkflows.isNotEmpty()) {
                val currentLatestWorkflow = pendingWorkflows[0]
                val currentStep = currentLatestWorkflow.currentStep
                if (currentStep >= Flow_Client_3.step) {
                    orchestrationClientCallService.shareLocalModelTaskToRemoteOrchestrationServer(currentLatestWorkflow.workflowTraceId!!)
                }
            }
        }
        return createClientRunModel(clientRunModeEntity)
    }
    /**
     * Explicitly sets the workflow run mode to MANUAL.
     *
     * @param modeId The ID of the run mode record to update.
     * @return The updated `ClientRunModel`.
     */
    @Transactional
    fun setWorkflowManualRunModel(modeId: Long): ClientRunModel {
        // Retrieve the current client run model
        val clientRunModel = workflowModelLogsService.getClientRunModelById(modeId)
        // Get the current mode from the clientRunModel
        val currentMode = RunModeOptionsEnum.fromString(clientRunModel.mode)
        if (currentMode == RunModeOptionsEnum.MANUAL) {
            return createClientRunModel(clientRunModel)
        }
        clientRunModel.mode = RunModeOptionsEnum.MANUAL.mode
        workflowModelLogsService.save(clientRunModel)
        return createClientRunModel(clientRunModel)
    }
    /**
     * Toggles the global weight update run mode between AUTO and MANUAL.
     *
     * @param modeId The ID of the run mode record to toggle.
     * @return The updated `ClientRunModel`.
     */
    fun toggleGlobalWeightRunModel(modeId: Long): ClientRunModel {
        val clientRunModeEntity = toggleModeAndSave(modeId)
        return createClientRunModel(clientRunModeEntity)
    }

    private fun toggleModeAndSave(modeId: Long): ClientRunModeEntity {
        val clientRunModel = workflowModelLogsService.getClientRunModelById(modeId)
        val currentMode = RunModeOptionsEnum.fromString(clientRunModel.mode)
        logger.info("Current mode: $currentMode")

        val newMode = when (currentMode) {
            RunModeOptionsEnum.MANUAL -> RunModeOptionsEnum.AUTO
            RunModeOptionsEnum.AUTO -> RunModeOptionsEnum.MANUAL
        }
        clientRunModel.mode = newMode.mode
        logger.info("Toggled mode: ${clientRunModel.mode}")
        return workflowModelLogsService.save(clientRunModel)
    }
    /**
     * Fetches and aggregates SHAP (SHapley Additive exPlanations) values for a given batch,
     * to be used in model explainability visualizations.
     *
     * @param domain The learning domain.
     * @param batchId The ID of the data batch.
     * @return A list of `ShapleyValues`, where each object contains a feature name and a list of its corresponding SHAP values.
     */
    fun getShapExplainerValues(domain: String, batchId: String): List<ShapleyValues> {
        val query =
            "SELECT shapley_values FROM fedlearn_client.model_predict_shap_data WHERE domain = '$domain' AND batch_id = '$batchId';"
        val shapItems = flClientQueryRepository.nativeQueryForResultList(query, String::class.java).toList();
        val retShapValues = ArrayList<ShapleyValues>()
        for (item in shapItems) {
            val attributes = JSONObject(item.toString());
            for (key in attributes.keys()) {
                var iter = retShapValues.find { sv -> sv.name == key }
                if (iter == null) {
                    retShapValues.add(ShapleyValues(key, ArrayList()));
                    iter = retShapValues.find { sv -> sv.name == key }
                }
                iter?.values?.add(attributes.getFloat(key));
            }
        }
        return retShapValues;
    }
}