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
        return SummaryData.headerNames.joinToString(separator = "", postfix = " |") { printColumn(it) }
    }

    private fun printSummary(sd: SummaryData): String {
        return sd.printableValues.joinToString(separator = "", postfix = " |") { printColumn(it) }
    }

    private fun printRequests(summary: ScenarioSummary): String {
        return summary.summaries.joinToString(separator = "\n") { printSummary(it) }
    }

    private fun printColumn(value: String): String {
        return "| ${value.padEnd(15, ' ').substring(0, 15)} "
    }
}
