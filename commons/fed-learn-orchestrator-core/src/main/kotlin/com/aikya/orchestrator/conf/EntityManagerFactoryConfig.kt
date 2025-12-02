package com.aikya.orchestrator.conf

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter


@Configuration
class EntityManagerFactoryConfig {
    @Bean
    fun entityManagerFactoryBuilder(): EntityManagerFactoryBuilder {
        return EntityManagerFactoryBuilder(HibernateJpaVendorAdapter(), HashMap<String, Any?>(), null)
    }
}