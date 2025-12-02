package com.aikya.orchestrator

import com.aikya.orchestrator.conf.ClientConf
import com.aikya.orchestrator.conf.WebPropConfig
import com.aikya.orchestrator.service.common.CallerService
import com.aikya.orchestrator.utils.AppConstants.ANSI_GREEN
import com.aikya.orchestrator.utils.AppConstants.ANSI_RED
import com.aikya.orchestrator.utils.AppConstants.ANSI_RESET
import com.aikya.orchestrator.utils.AppUtils.failure
import com.aikya.orchestrator.utils.AppUtils.success
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import kotlin.system.exitProcess


@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(WebPropConfig::class)
@EntityScan("com.aikya")
class FedLearnOrchestratorClient(private val clientConf: ClientConf) : CommandLineRunner {
    private val logger: Logger = LoggerFactory.getLogger(FedLearnOrchestratorClient::class.java)

    @Autowired
    private lateinit var callerService: CallerService

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FedLearnOrchestratorClient::class.java, *args)
        }
    }

    override fun run(vararg args: String?) {
        val serviceDependencies = clientConf.getServiceDependencyUrls()
        val unhealthyServices =
            serviceDependencies.filterNot { (name, url) -> callerService.checkDependencyServiceHealth(name, url) }
        if (unhealthyServices.isEmpty()) {
            success(
                logger,
                "${ANSI_GREEN}All dependency services are up. Starting FedLearnOrchestratorClient...$ANSI_RESET"
            )
        } else {
            unhealthyServices.forEach { (name, url) ->
                failure(logger, "${ANSI_RED}Dependency service not running: $name ($url)$ANSI_RESET")
            }
            exitProcess(1)
        }
    }
}