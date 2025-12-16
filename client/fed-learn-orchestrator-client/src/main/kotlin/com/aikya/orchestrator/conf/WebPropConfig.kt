package com.aikya.orchestrator.conf

import com.aikya.orchestrator.dto.common.DashboardConfig
import com.aikya.orchestrator.dto.common.UrlConfig
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
class WebPropConfig {
    private val logger: Logger = LoggerFactory.getLogger(WebPropConfig::class.java)
    var dashboard: List<DashboardConfig> = listOf()
    var url: List<UrlConfig> = listOf()
    lateinit var node: Node

    class Node {
        lateinit var name: String
        var number: Int = 0
    }

    @PostConstruct
    fun init() {
        check(url.isNotEmpty()) { "URL configuration not loaded or is empty" }

        // Check initialization and validity of UrlConfig properties
        url.forEach {
            check(it.name.isNotBlank()) { "UrlConfig name is not initialized or is empty" }
            check(it.baseUrl.isNotBlank()) { "UrlConfig baseUrl is not initialized or is empty" }
            check(it.mapping.isNotEmpty()) { "UrlConfig mapping is not initialized or is empty" }
        }
        check(node.name.isNotBlank()) { "Node configuration not loaded or name is empty" }

        // Print all loaded URLs
        url.forEach {
            logger.info("Loaded URL config: name=${it.name}, baseUrl=${it.baseUrl}, mapping=${it.mapping}")
        }
    }
}
