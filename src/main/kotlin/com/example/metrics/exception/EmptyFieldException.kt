package com.example.metrics.exception

import com.example.metrics.logging.Logger

interface EmptyFieldException : Logger {
    fun getEmptyFieldMessage(fieldName: String): String {
        info("Empty field detected: $fieldName")
        return "The field '$fieldName' is empty."
    }

}
