package com.aikya.orchestrator.service.common

import org.springframework.beans.factory.annotation.Autowired

open class BaseService @Autowired constructor(val queryLoaderService: QueryLoaderService)