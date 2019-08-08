package io.perfometer.statistics.printer

import io.perfometer.statistics.ScenarioSummary

internal interface StatisticsSummaryPrinter {

    fun print(summary: ScenarioSummary)
}
