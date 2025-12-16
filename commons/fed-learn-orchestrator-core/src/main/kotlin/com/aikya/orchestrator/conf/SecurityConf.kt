package com.aikya.orchestrator.conf
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
@Configuration
@EnableWebSecurity
@Suppress("DEPRECATION")
class KotlinSecurityConfiguration {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }
            .cors().disable()
            .csrf().disable()
            .httpBasic().disable()  // Explicitly disable HTTP Basic authentication
            .formLogin().disable()  // Disable form login if not already disabled

        return http.build()
    }
}