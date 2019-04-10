package io.perfometer.statistics

import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 * @author Tomasz Tarczyński
 */
internal class ConcurrentQueueScenarioStatistics(override val startTime: Instant) : ScenarioStatistics {
    private val scenarioStats = ConcurrentLinkedQueue<Statistics>()

    override lateinit var endTime: Instant

    override fun getSummary(): ScenarioSummary {
        return ScenarioSummary(scenarioStats, startTime, endTime)
    }

    override fun gather(statistics: Statistics) {
        this.scenarioStats.add(statistics)
    }

}
