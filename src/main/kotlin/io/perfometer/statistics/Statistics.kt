package io.perfometer.statistics

import io.perfometer.http.HttpStatus
import java.time.Duration
import java.time.Instant

/**
 * Set of data classes representing different statistics collected during scenario life cycle.
 *
 * @author Tomasz Tarczyński
 */
internal sealed class Statistics

internal data class RequestStatistics(val timeTaken: Duration,
                                      val httpStatus: HttpStatus) : Statistics()

internal data class PauseStatistics(val duration: Duration) : Statistics()

internal data class ScenarioSummary(val statistics: Collection<Statistics>,
                                    private val startTime: Instant,
                                    private val endTime: Instant) {

    fun hasRequests() = statistics.any { it is RequestStatistics }

    val slowestRequest: RequestStatistics?
        get() = this.statistics
                .filter { it is RequestStatistics }
                .map { it as RequestStatistics }
                .maxBy { it.timeTaken }

    val fastestRequest: RequestStatistics?
        get() = this.statistics
                .filter { it is RequestStatistics }
                .map { it as RequestStatistics }
                .minBy { it.timeTaken }

    val meanAverageRequestTime: Duration
        get() = this.statistics
                .filter { it is RequestStatistics }
                .map { it as RequestStatistics }
                .map { it.timeTaken.toMillis() }
                .average()
                .let { Duration.ofMillis(it.toLong()) }

    val scenarioTime: Duration
        get() = Duration.between(startTime, endTime)
}
