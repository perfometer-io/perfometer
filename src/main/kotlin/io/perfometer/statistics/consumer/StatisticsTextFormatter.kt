package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toReadableString
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.SummaryData

internal object StatisticsTextFormatter {

    fun format(summary: ScenarioSummary): String {
        return if (summary.totalSummary == null) {
            "===> No requests run! <==="
        } else {
            """Scenario Duration: ${summary.scenarioDuration.toReadableString()}
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
        ).joinToString(separator = "", postfix = " |") { printColumn(it) }
    }

    private fun printSummary(sd: SummaryData): String {
        return arrayOf(
            sd.name,
            sd.requestCount.toString(),
            sd.failedRequestCount.toString(),
            sd.fastestTime.toReadableString(),
            sd.averageTime.toReadableString(),
            sd.percentile95Time.toReadableString(),
            sd.percentile96Time.toReadableString(),
            sd.percentile97Time.toReadableString(),
            sd.percentile98Time.toReadableString(),
            sd.percentile99Time.toReadableString(),
            sd.slowestTime.toReadableString()
        ).joinToString(separator = "", postfix = " |") { printColumn(it) }
    }

    private fun printRequests(summary: ScenarioSummary): String {
        return summary.summaries.joinToString(separator = "\n") { printSummary(it) }
    }

    private fun printColumn(value: String): String {
        return "| ${value.padEnd(15, ' ').substring(0, 15)} "
    }
}
