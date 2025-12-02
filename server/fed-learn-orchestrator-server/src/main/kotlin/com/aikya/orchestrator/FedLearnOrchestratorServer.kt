package com.aikya.orchestrator

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
@EntityScan("com.aikya")
class FedLearnOrchestratorServer: CommandLineRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FedLearnOrchestratorServer::class.java, *args)
        }
    }

    override fun run(vararg args: String?) {
        // No initialization required at startup
    }
}