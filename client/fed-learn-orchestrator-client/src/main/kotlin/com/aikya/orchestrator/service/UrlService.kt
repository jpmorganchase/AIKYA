package com.aikya.orchestrator.service

import com.aikya.orchestrator.conf.WebPropConfig
import org.springframework.stereotype.Service
/**
 * Service responsible for resolving and constructing URLs for different categories
 * such as `auth`, `data`, `agent`, `server`, and `client` based on the application configuration.
 *
 * @param webPropConfig Configuration object containing base URLs and mappings for different services.
 */
@Service
class UrlService(val webPropConfig: WebPropConfig) {
    /**
     * Retrieves a fully constructed URL for the given category and type.
     * Replaces `{node}` placeholder with the current node name.
     *
     * @param category The category of the URL (e.g., "auth", "data").
     * @param type The type of URL within the category (e.g., "login", "register").
     * @return The fully constructed URL.
     * @throws IllegalArgumentException if category or type is not found in the configuration.
     */
    fun getUrl(category: String, type: String): String {
        val urlConfig = webPropConfig.url.find { it.name == category }
            ?: throw IllegalArgumentException("Unknown URL category: $category")

        val subUrl = urlConfig.mapping[type]
            ?: throw IllegalArgumentException("Unknown URL type: $type")

        return urlConfig.baseUrl + subUrl.replace("{node}", webPropConfig.node.name)
    }
    /**
     * Retrieves an authentication-related URL for the given type.
     *
     * @param type The specific type of auth URL (e.g., "login", "logout").
     * @return The constructed auth URL.
     */
    fun getAuthUrl(type: String): String {
        return getUrl("auth", type)
    }
    /**
     * Retrieves a data-related URL for the given type.
     *
     * @param type The specific type of data URL.
     * @return The constructed data URL.
     */
    fun getDataUrl(type: String): String {
        return getUrl("data", type)
    }
    /**
     * Retrieves an agent-related URL for the given type.
     *
     * @param type The specific type of agent URL.
     * @return The constructed agent URL.
     */
    fun getAgentUrl(type: String): String {
        return getUrl("agent", type)
    }
    /**
     * Retrieves a server-related URL for the given type.
     *
     * @param type The specific type of server URL.
     * @return The constructed server URL.
     */
    fun getServerUrl(type: String): String {
        return getUrl("server", type)
    }
    /**
     * Retrieves a server-related URL for the given type, replacing placeholders with provided parameters.
     *
     * @param type The type of server URL.
     * @param params A vararg of key-value pairs to replace placeholders in the URL.
     * @return The constructed and parameterized server URL.
     */
    fun getServerUrl(type: String, vararg params: Pair<String, String>): String {
        var url = getUrl("server", type)
        for (param in params) {
            url = url.replace("{${param.first}}", param.second)
        }
        return url
    }
    /**
     * Retrieves a client-related URL for the given type.
     *
     * @param type The specific type of client URL.
     * @return The constructed client URL.
     */
    fun getClientUrl(type: String): String {
        return getUrl("client", type)
    }
    /**
     * Retrieves a client-related URL for the given type, replacing placeholders with provided parameters.
     *
     * @param type The type of client URL.
     * @param params A vararg of key-value pairs to replace placeholders in the URL.
     * @return The constructed and parameterized client URL.
     */
    fun getClientUrl(type: String, vararg params: Pair<String, String>): String {
        var url = getUrl("client", type)
        for (param in params) {
            url = url.replace("{${param.first}}", param.second)
        }
        return url
    }
    /**
     * Retrieves the current node name from configuration.
     *
     * @return The name of the current node.
     */
    fun getCurrentNode(): String {
        return webPropConfig.node.name
    }
    /**
     * Retrieves the numeric identifier of the current node.
     *
     * @return The number assigned to the current node.
     */
    fun getCurrentNodeNumber(): Int {
        return webPropConfig.node.number
    }
}