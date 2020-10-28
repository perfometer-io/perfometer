package io.perfometer.statistics.consumer

import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.SummaryData
import java.time.Duration

internal object StatisticsTextFormatter {

    fun format(summary: ScenarioSummary): String {
        return if (summary.totalSummary == null) {
            "===> No requests run! <==="
        } else {
            """Scenario Duration: ${formatDuration(summary.scenarioDuration)}
                +${printHeader()}
                +${printSummary(summary.totalSummary)}
                +${printRequests(summary)}
            """.trimMargin(marginPrefix = "+")
        }
    }

    private fun printHeader(): String {
        return arrayOf(
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
        ).joinToString(separator = "") { printColumn(it) }
    }

    private fun printSummary(sd: SummaryData): String {
        return arrayOf(
            sd.name,
            sd.requestCount.toString(),
            sd.failedRequestCount.toString(),
            formatDuration(sd.fastestTime),
            formatDuration(sd.averageTime),
            formatDuration(sd.percentile95Time),
            formatDuration(sd.percentile96Time),
            formatDuration(sd.percentile97Time),
            formatDuration(sd.percentile98Time),
            formatDuration(sd.percentile99Time),
            formatDuration(sd.slowestTime)
        ).joinToString(separator = "") { printColumn(it) }
    }

    private fun printRequests(summary: ScenarioSummary): String {
        return summary.summaries.joinToString(separator = "\n") { printSummary(it) }
    }

    private fun printColumn(value: String): String {
        return "| ${value.padEnd(15, ' ').substring(0, 15)} "
    }

    private fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val positive = String.format(
            "%d:%02d:%02d.%03d",
            duration.toHours(),
            duration.toMinutes() % 60,
            duration.seconds % 60,
            duration.toMillis() % 1000
        )
        return if (seconds < 0) "-$positive" else positive
    }

}
