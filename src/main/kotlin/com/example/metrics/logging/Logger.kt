package com.example.metrics.logging

import org.slf4j.LoggerFactory

interface Logger {
    val logger: org.slf4j.Logger
        get() = LoggerFactory.getLogger(this::class.java)

    fun info(message: String) {
        logger.info(message)
    }

    fun warn(message: String) {
        logger.warn(message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message)
        }
    }
}
