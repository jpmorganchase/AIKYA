package com.aikya.orchestrator.conf


import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.aikya.orchestrator.aggregate", "com.aikya.orchestrator.shared"],
    entityManagerFactoryRef = "aggregateEntityManagerFactory",
    transactionManagerRef = "aggregateTransactionManager"
)
class AggregateDataSourceConfig {
    @Bean(name = ["aggregateDataSource"])
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.aggregate")
    fun aggregateDataSource(): DataSource? {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }


    @Bean(name = ["aggregateEntityManagerFactory"])
    @Primary
    fun aggregateEntityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        @Qualifier("aggregateDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val properties = HashMap<String, Any>()
        properties["hibernate.hbm2ddl.auto"] = "none"
        properties["hibernate.dialect"] = "org.hibernate.dialect.MySQLDialect"
        properties["hibernate.show_sql"] = false
        properties["hibernate.format_sql"] = false
        properties["hibernate.id.new_generator_mappings"] = false
        return builder
            .dataSource(dataSource)
            .packages("com.aikya.orchestrator.aggregate", "com.aikya.orchestrator.shared")
            .persistenceUnit("aggregate")
            .properties(properties)
            .build()

    }

    @Bean(name = ["aggregateTransactionManager"])
    @Primary
    fun aggregateTransactionManager(
        @Qualifier("aggregateEntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}