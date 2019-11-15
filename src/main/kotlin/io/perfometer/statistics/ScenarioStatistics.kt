package io.perfometer.statistics

internal interface ScenarioStatistics {

    /**
     * Aggregates scenario's statistic objects. The implementations of this method should be
     * thread safe and non-blocking to behave well under multithreaded, high performance environments.
     *
     * @param statistics
     */
    fun gather(statistics: Statistics)

    fun finish(): ScenarioSummary
}
