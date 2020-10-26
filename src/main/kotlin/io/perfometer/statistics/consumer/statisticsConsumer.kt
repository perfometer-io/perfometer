package io.perfometer.statistics.consumer

import io.perfometer.statistics.ScenarioSummary

internal fun getStatisticsConsumer(output: Output): (ScenarioSummary) -> Unit {
    return when (output) {
        Output.STDOUT -> { summary: ScenarioSummary -> StdOutStatisticsPrinter().print(summary) }
        Output.TEXT_FILE -> { summary: ScenarioSummary -> devNull(summary) }
        Output.PDF -> { summary: ScenarioSummary -> devNull(summary) }
        Output.HTML -> { summary: ScenarioSummary -> devNull(summary) }
    }
}

private fun devNull(scenarioSummary: ScenarioSummary) {
    // not implemented
}
