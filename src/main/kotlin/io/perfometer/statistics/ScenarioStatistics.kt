package io.perfometer.statistics

import java.time.Instant

internal interface ScenarioStatistics {

    /**
     * Aggregates scenario's statistic objects. The implementations of this method should be
     * thread safe and non-blocking to behave well under multithreaded, high performance environments.
     *
     * @param statistics
     */
    fun gather(statistics: Statistics)

    /**
     * Returns a ScenarioSummary object build upon statistics gathered via the gather method.
     *
     */
    fun getSummary(): ScenarioSummary

    val startTime: Instant

    var endTime: Instant
}
