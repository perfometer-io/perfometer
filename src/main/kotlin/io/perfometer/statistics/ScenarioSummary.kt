package io.perfometer.statistics

import io.perfometer.internal.helper.toReadableString
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
) {

    val printableValues: Array<String> = arrayOf(
        name,
        requestCount.toString(),
        failedRequestCount.toString(),
        fastestTime.toReadableString(),
        averageTime.toReadableString(),
        percentile95Time.toReadableString(),
        percentile96Time.toReadableString(),
        percentile97Time.toReadableString(),
        percentile98Time.toReadableString(),
        percentile99Time.toReadableString(),
        slowestTime.toReadableString()
    )

    fun <R> mapPrintableValues(valueMapper: (String) -> R): List<R> = printableValues.map(valueMapper)

    companion object {
        val headerNames: Array<String> = arrayOf(
            "REQUEST",
            "COUNT",
            "FAILED COUNT",
            "FASTEST TIME",
            "AVERAGE TIME",
            "95th PERCENTILE",
            "96th PERCENTILE",
            "97th PERCENTILE",
            "98th PERCENTILE",
            "99th PERCENTILE",
            "SLOWEST TIME"
        )

        fun <R> mapHeaders(headerMapper: (String) -> R): List<R> = headerNames.map(headerMapper)
    }
}

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
