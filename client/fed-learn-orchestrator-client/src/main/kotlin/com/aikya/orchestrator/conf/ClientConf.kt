package com.aikya.orchestrator.conf

import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class ClientConf {
    @Value("\${app.service-dependency.data-processor}")
    private lateinit var dataProcessorHealthUrl: String

    @Value("\${app.service-dependency.fl-agent}")
    private lateinit var flAgentHealthUrl: String

    @Value("\${app.service-dependency.fl-server-orchestrator}")
    private lateinit var flServerOrchestratorHealthUrl: String


    fun getServiceDependencyUrls(): List<Pair<String, String>> {
        return listOf(
            "data-processor server" to dataProcessorHealthUrl,
            "fl-agent server" to flAgentHealthUrl,
            "fl-server server" to flServerOrchestratorHealthUrl
        )
    }

    @Bean
    fun customWebClient(): WebClient {
        val sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()

        val httpClient = HttpClient.create().secure { t -> t.sslContext(sslContext) }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}