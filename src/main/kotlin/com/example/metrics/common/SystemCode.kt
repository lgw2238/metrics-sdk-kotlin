package com.example.metrics.common

enum class SystemCode(private val message: String) {
    SUCCESS("Operation completed successfully"),
    FAILED("Api Transmission failed"),
    CANCELED("API transmission has been canceled"),
    ERROR("An error occurred"),
    NOT_FOUND("Resource not found"),
    UNAUTHORIZED("Unauthorized access"),
    FORBIDDEN("Access is forbidden");

    fun getMessage(): String {
        return message
    }
}
