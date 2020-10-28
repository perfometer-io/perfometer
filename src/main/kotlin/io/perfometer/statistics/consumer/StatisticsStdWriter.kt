package io.perfometer.statistics.consumer

import io.perfometer.statistics.ScenarioSummary

internal object StatisticsStdWriter {

    fun write(summary: ScenarioSummary) {
        println(StatisticsTextFormatter.format(summary))
    }
}
