package io.perfometer.statistics.consumer

import io.perfometer.statistics.ScenarioSummary

internal object StatisticsStdOutWriter {

    fun write(summary: ScenarioSummary) {
        println(StatisticsFormatter.formatAsText(summary))
    }
}
