package io.perfometer.printer

import io.perfometer.statistics.ScenarioSummary

internal interface StatisticsPrinter {

    fun print(scenarioSummary: ScenarioSummary)
}
