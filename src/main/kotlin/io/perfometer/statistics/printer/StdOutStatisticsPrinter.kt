package io.perfometer.statistics.printer

import io.perfometer.statistics.ScenarioSummary
import java.time.Duration

internal class StdOutStatisticsPrinter : StatisticsPrinter {

    override fun print(scenarioSummary: ScenarioSummary) {
        if (!scenarioSummary.hasRequests()) {
            println("No requests run!")
        } else {
            val fastest = scenarioSummary.fastestRequest!!
            val slowest = scenarioSummary.slowestRequest!!
            println("Scenario Duration: %s\nFastest request time: %s, %d\nSlowest request: %s, %d"
                    .format(formatDuration(scenarioSummary.scenarioTime),
                            formatDuration(fastest.timeTaken),
                            fastest.httpStatus.code,
                            formatDuration(slowest.timeTaken),
                            slowest.httpStatus.code))
        }
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
