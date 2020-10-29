package io.perfometer.statistics.consumer

import io.perfometer.statistics.ScenarioSummary

fun consumeStatistics(
    summary: ScenarioSummary,
    vararg outputs: Output,
) = outputs.forEach { getStatisticsConsumer(it)(summary) }

private fun getStatisticsConsumer(output: Output): (ScenarioSummary) -> Unit {
    return when (output) {
        Output.STDOUT -> { summary: ScenarioSummary -> StatisticsStdOutWriter.write(summary) }
        Output.TEXT_FILE -> { summary: ScenarioSummary -> StatisticsTextFileWriter.write(summary) }
        Output.PDF -> { _: ScenarioSummary -> devNull() }
        Output.HTML -> { summary: ScenarioSummary -> StatisticsHtmlFileWriter.write(summary) }
    }
}

private fun devNull() {
    TODO("Not yet implemented")
}
