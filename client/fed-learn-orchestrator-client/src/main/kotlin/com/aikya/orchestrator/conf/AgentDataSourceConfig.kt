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
    basePackages = ["com.aikya.orchestrator.agent", "com.aikya.orchestrator.shared", "com.aikya.orchestrator.repository.agent"],
    entityManagerFactoryRef = "agentEntityManagerFactory",
    transactionManagerRef = "agentTransactionManager"
)
class AgentDataSourceConfig {
    @Bean(name = ["agentDataSource"])
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.agent")
    fun agentDataSource(): DataSource? {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }


    @Bean(name = ["agentEntityManagerFactory"])
    @Primary
    fun agentEntityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        @Qualifier("agentDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val properties = HashMap<String, Any>()
        properties["hibernate.hbm2ddl.auto"] = "none"
        properties["hibernate.dialect"] = "org.hibernate.dialect.MySQLDialect"
        properties["hibernate.show_sql"] = false
        properties["hibernate.format_sql"] = false
        properties["hibernate.id.new_generator_mappings"] = false
        return builder
            .dataSource(dataSource)
            .packages("com.aikya.orchestrator.agent", "com.aikya.orchestrator.shared")
            .persistenceUnit("agent")
            .properties(properties)
            .build()

    }

    @Bean(name = ["agentTransactionManager"])
    @Primary
    fun agentTransactionManager(
        @Qualifier("agentEntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}