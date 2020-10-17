package io.perfometer.statistics

import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

internal class ConcurrentQueueScenarioStatistics(
        private val startTime: Instant,
) : ScenarioStatistics {
    private val scenarioStats = ConcurrentLinkedQueue<Statistics>()
    private var finished = false

    override fun finish(): ScenarioSummary {
        finished = true
        return ScenarioSummary(scenarioStats, startTime, Instant.now())
    }

    override fun gather(statistics: Statistics) {
        check(!finished) { "Scenario already finished" }
        this.scenarioStats.add(statistics)
    }
}
