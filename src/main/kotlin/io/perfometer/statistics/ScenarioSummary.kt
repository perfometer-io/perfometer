package io.perfometer.statistics

import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

data class SummaryData(
    val name: String,
    val requestCount: Int,
    val failedRequestCount: Int,
    val fastestTime: Duration,
    val averageTime: Duration,
    val percentile95Time: Duration,
    val percentile96Time: Duration,
    val percentile97Time: Duration,
    val percentile98Time: Duration,
    val percentile99Time: Duration,
    val slowestTime: Duration,
)

class ScenarioSummary(
    statistics: Collection<Statistics>,
    val startTime: Instant,
    val endTime: Instant,
) {
    private val requestStatistics = statistics.filterIsInstance<RequestStatistics>()
    val scenarioDuration: Duration = Duration.between(startTime, endTime)
    val totalSummary = if (requestStatistics.isEmpty()) null
    else generateSummary("TOTAL", requestStatistics)
    val summaries = requestStatistics
        .groupBy { it.name }
        .map {
            generateSummary(it.key, it.value)
        }

    private fun generateSummary(
        name: String,
        statistics: Collection<RequestStatistics>
    ): SummaryData {
        val sortedTimes = statistics.map { it.timeTaken }.sorted()
        return SummaryData(
            name,
            statistics.count(),
            statistics.filter { !it.httpStatus.isSuccess }.count(),
            sortedTimes.first(),
            statistics.map { it.timeTaken.toMillis() }.average()
                .let { Duration.ofMillis(it.toLong()) },
            sortedTimes[ceil(sortedTimes.size * 0.95).toInt() - 1],
            sortedTimes[ceil(sortedTimes.size * 0.96).toInt() - 1],
            sortedTimes[ceil(sortedTimes.size * 0.97).toInt() - 1],
            sortedTimes[ceil(sortedTimes.size * 0.98).toInt() - 1],
            sortedTimes[ceil(sortedTimes.size * 0.99).toInt() - 1],
            sortedTimes.last(),
        )
    }
}
