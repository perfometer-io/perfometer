package io.perfometer.statistics

interface ScenarioStatistics {

    fun gather(statistics: Statistics): ScenarioStatistics

    fun finish(): ScenarioSummary
}
