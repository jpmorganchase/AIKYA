package com.aikya.orchestrator.conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableScheduling
class AppConf {

    @Bean
    fun webClient(): WebClient? {
        return WebClient.builder().build()
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val kotlinModule = KotlinModule.Builder().build()
        return Jackson2ObjectMapperBuilder.json()
            .modules(kotlinModule)
            .build()
    }
    @Bean("asyncTaskScheduler")
    fun asyncTaskScheduler(): ThreadPoolTaskScheduler {
        return ThreadPoolTaskScheduler().apply {
            poolSize = 15
            setThreadNamePrefix("AsyncThreadPoolTaskScheduler")
            initialize()
        }
    }
}