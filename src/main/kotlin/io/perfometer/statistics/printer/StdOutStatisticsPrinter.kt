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
            arrayOf("REQUEST",
                    "COUNT",
                    "FAILED COUNT",
                    "FASTEST TIME",
                    "AVERAGE TIME",
                    "95th PERCENTILE",
                    "96th PERCENTILE",
                    "97th PERCENTILE",
                    "98th PERCENTILE",
                    "99th PERCENTILE",
                    "SLOWEST TIME").forEach { printColumn(it) }
            println("|")
            printSummary(scenarioSummary.totalSummary)
            scenarioSummary.summaries.forEach{
                printSummary(it)
            }
        }
    }

    private fun printSummary(it: SummaryData) {
        arrayOf(it.name,
                it.requestCount.toString(),
                it.failedRequestCount.toString(),
                formatDuration(it.fastestTime),
                formatDuration(it.averageTime),
                formatDuration(it.percentile95Time),
                formatDuration(it.percentile96Time),
                formatDuration(it.percentile97Time),
                formatDuration(it.percentile98Time),
                formatDuration(it.percentile99Time),
                formatDuration(it.slowestTime)).forEach { printColumn(it) }
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
