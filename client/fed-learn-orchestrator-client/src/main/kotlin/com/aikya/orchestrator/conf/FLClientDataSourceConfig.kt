package com.aikya.orchestrator.conf

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.aikya.orchestrator.client", "com.aikya.orchestrator.repository.client"],
    entityManagerFactoryRef = "clientEntityManagerFactory",
    transactionManagerRef = "clientTransactionManager"
)
class FLClientDataSourceConfig {
    @Bean(name = ["clientDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.client")
    fun clientDataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean(name = ["clientEntityManagerFactory"])
    fun clientEntityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        @Qualifier("clientDataSource") dataSource: DataSource?
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("com.aikya.orchestrator.client")
            .persistenceUnit("client")
            .properties(hibernateProperties())
            .build()
    }

    @Bean(name = ["clientTransactionManager"])
    fun clientTransactionManager(
        @Qualifier("clientEntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }

    private fun hibernateProperties(): Map<String, Any> {
        val properties: MutableMap<String, Any> = HashMap()
        properties["hibernate.hbm2ddl.auto"] = "none"
        properties["hibernate.dialect"] = "org.hibernate.dialect.MySQLDialect"
        properties["hibernate.show_sql"] = "false"
        properties["hibernate.format_sql"] = "false"
        properties["hibernate.id.new_generator_mappings"] = "false"
        return properties
    }
}