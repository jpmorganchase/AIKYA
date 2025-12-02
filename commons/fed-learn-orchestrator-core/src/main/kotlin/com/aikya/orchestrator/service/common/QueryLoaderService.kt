package com.aikya.orchestrator.service.common

import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * A Spring service responsible for loading SQL queries from `.sql` files at application startup.
 *
 * This service scans the `classpath:/query/` directory for files with a `.sql` extension.
 * It parses these files to extract named SQL queries and stores them in an in-memory map.
 * Queries within the files must be delineated by a special comment format: `--#query=<query_name>`.
 * This allows multiple queries to be stored in a single file.
 *
 * @property resourceResolver A Spring resolver used to find and load the SQL query files.
 */
@Service
class QueryLoaderService (var resourceResolver: ResourcePatternResolver){
    private val queryMap: MutableMap<String, String> = mutableMapOf()

    init {
        loadQueries()
    }
    /**
     * Loads SQL queries from .sql files located in the classpath:/query directory into the queryMap.
     * Each query is associated with its respective name extracted from the SQL file.
     */
    private fun loadQueries() {
        val resources = resourceResolver.getResources("classpath:/query/*.sql")
        for (resource in resources) {
            val reader = BufferedReader(InputStreamReader(resource.inputStream))
            var currentQueryName: String? = null
            val stringBuilder = StringBuilder()
            reader.forEachLine { line ->
                if (line.startsWith("--#query=")) {
                    // If there's a new query name, save the previous query content to the map
                    currentQueryName?.let {
                        queryMap[it] = stringBuilder.toString().trim()
                        stringBuilder.clear()
                    }
                    // Extract the new query name
                    currentQueryName = line.substringAfter("--#query=").trim()
                } else {
                    // Append to the current query content
                    stringBuilder.append(line).append("\n")
                }
            }
            // Save the last query content to the map
            currentQueryName?.let {
                queryMap[it] = stringBuilder.toString().trim()
                stringBuilder.clear()
            }
        }
    }
    fun getQuery(queryName: String): String {
        return queryMap[queryName] ?: throw NoSuchElementException("Query $queryName not found")
    }
}