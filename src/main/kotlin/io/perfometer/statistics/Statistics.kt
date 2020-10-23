package io.perfometer.statistics

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Set of data classes representing different statistics collected during scenario life cycle.
 *
 * @author Tomasz Tarczyński
 */
sealed class Statistics

data class RequestStatistics(
        val method: HttpMethod,
        val pathWithParams: String,
        val timeTaken: Duration,
        val httpStatus: HttpStatus,
) : Statistics()

internal data class PauseStatistics(
        val duration: Duration,
) : Statistics()

class ScenarioSummary(
        statistics: Collection<Statistics>,
        private val startTime: Instant,
        private val endTime: Instant,
) {
    private val _statistics = statistics

    val statistics: Collection<Statistics>
        get() = Collections.unmodifiableCollection(_statistics)

    fun hasRequests() = statistics.any { it is RequestStatistics }

    val slowestRequest: RequestStatistics
        get() = this.statistics
                .filterIsInstance<RequestStatistics>()
                .sortedBy { it.timeTaken }
                .last()

    val fastestRequest: RequestStatistics
        get() = this.statistics
                .filterIsInstance<RequestStatistics>()
                .sortedBy { it.timeTaken }
                .first()

    val meanAverageRequestTime: Duration
        get() = this.statistics
                .filterIsInstance<RequestStatistics>()
                .map { it.timeTaken.toMillis() }
                .average()
                .let { Duration.ofMillis(it.toLong()) }

    val scenarioTime: Duration
        get() = Duration.between(startTime, endTime)
}
