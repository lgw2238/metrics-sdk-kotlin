package com.metrics.sdk.model

data class MetricsData(
    val id: String,
    val name: String,
    val rows: Int,
    val columns: Int,
    val data: List<List<Double>>,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Validate metrics data integrity
     */
    fun validate(): Result<Unit> {
        if (rows <= 0 || columns <= 0) {
            return Result.failure(IllegalArgumentException("Metrics dimensions must be positive"))
        }

        if (data.size != rows) {
            return Result.failure(IllegalArgumentException("Data rows count doesn't match declared rows"))
        }

        data.forEachIndexed { index, row ->
            if (row.size != columns) {
                return Result.failure(IllegalArgumentException("Row $index size doesn't match declared columns"))
            }
        }

        return Result.success(Unit)
    }

    /**
     * Get metrics element at position
     */
    fun get(row: Int, col: Int): Double? {
        return if (row in 0 until rows && col in 0 until columns) {
            data[row][col]
        } else null
    }

    /**
     * Create a copy with new data
     */
    fun withData(newData: List<List<Double>>): MetricsData {
        return copy(data = newData, timestamp = System.currentTimeMillis())
    }
}
