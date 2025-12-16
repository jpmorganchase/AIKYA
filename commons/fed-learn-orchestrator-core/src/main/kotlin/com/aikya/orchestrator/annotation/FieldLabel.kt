package com.aikya.orchestrator.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldLabel(val label: String, val order: Int)
