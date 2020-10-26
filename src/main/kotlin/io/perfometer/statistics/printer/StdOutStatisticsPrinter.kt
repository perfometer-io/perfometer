package io.perfometer.statistics.printer

import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.SummaryData
import java.time.Duration

internal class StdOutStatisticsPrinter : StatisticsPrinter {

    override fun print(scenarioSummary: ScenarioSummary) {
        if (scenarioSummary.totalSummary == null) {
            println("No requests run!")
        } else {
            println("Scenario Duration: %s\n".format(formatDuration(scenarioSummary.scenarioTime)))
            printColumn("REQUEST")
            printColumn("COUNT")
            printColumn("FAILED COUNT")
            printColumn("FASTEST TIME")
            printColumn("AVERAGE TIME")
            printColumn("95th PERCENTILE")
            printColumn("96th PERCENTILE")
            printColumn("97th PERCENTILE")
            printColumn("98th PERCENTILE")
            printColumn("99th PERCENTILE")
            printColumn("SLOWEST TIME")
            println("|")
            printSummary(scenarioSummary.totalSummary)
            scenarioSummary.summaries.forEach{
                printSummary(it)
            }
        }
    }

    private fun printSummary(it: SummaryData) {
        printColumn(it.name)
        printColumn(it.requestCount.toString())
        printColumn(it.failedRequestCount.toString())
        printColumn(formatDuration(it.fastestTime))
        printColumn(formatDuration(it.averageTime))
        printColumn(formatDuration(it.percentile95Time))
        printColumn(formatDuration(it.percentile96Time))
        printColumn(formatDuration(it.percentile97Time))
        printColumn(formatDuration(it.percentile98Time))
        printColumn(formatDuration(it.percentile99Time))
        printColumn(formatDuration(it.slowestTime))
        println("|")
    }

    private fun printColumn(value: String) {
        print("| ${value.padEnd(15, ' ').substring(0, 15)} ")
    }

    private fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val positive = String.format(
                "%d:%02d:%02d.%03d",
                duration.toHours(),
                duration.toMinutes() % 60,
                duration.seconds % 60,
                duration.toMillis() % 1000)
        return if (seconds < 0) "-$positive" else positive
    }

}
