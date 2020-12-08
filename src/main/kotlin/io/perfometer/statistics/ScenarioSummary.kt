package io.perfometer.statistics

import io.perfometer.internal.helper.toReadableString
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

data class SummaryData(
    val name: String,
    val rps: List<Int>,
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

    val minimumRps = rps.minOrNull() ?: 0
    val averageRps = rps.average().toInt()
    val maximumRps = rps.maxOrNull() ?: 0

    val printableValues: Array<String> = arrayOf(
        name,
        minimumRps.toString(),
        averageRps.toString(),
        maximumRps.toString(),
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
            "MINIMUM RPS",
            "AVERAGE RPS",
            "MAXIMUM RPS",
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
            calculateRps(statistics),
            statistics.count(),
            statistics.filter { !it.httpStatus.isSuccess }.count(),
            sortedTimes.first(),
            calculateAverageTime(statistics),
            calculatePercentile(sortedTimes, 0.95),
            calculatePercentile(sortedTimes, 0.96),
            calculatePercentile(sortedTimes, 0.97),
            calculatePercentile(sortedTimes, 0.98),
            calculatePercentile(sortedTimes, 0.99),
            sortedTimes.last(),
        )
    }

    private fun calculatePercentile(sortedTimes: List<Duration>, percentile: Double) =
        sortedTimes[ceil(sortedTimes.size * percentile).toInt() - 1]

    private fun calculateAverageTime(statistics: Collection<RequestStatistics>) =
        statistics
            .map { it.timeTaken.toMillis() }
            .average()
            .let { Duration.ofMillis(it.toLong()) }

    private fun calculateRps(statistics: Collection<RequestStatistics>): List<Int> =
        statistics
            .groupBy { it.endTime.epochSecond }
            .mapValues { it.value.size }
            .toList()
            .fold(
                intList((endTime.epochSecond - startTime.epochSecond).toInt() + 1),
                { list, rps -> list[(rps.first - startTime.epochSecond).toInt()] = rps.second; list }
            )

    private fun intList(size: Int): ArrayList<Int> {
        val intList = ArrayList<Int>(size)
        for (i in 1..size) intList.add(0)
        return intList
    }
}
