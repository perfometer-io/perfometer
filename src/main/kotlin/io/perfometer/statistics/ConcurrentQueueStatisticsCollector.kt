package io.perfometer.statistics

import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

internal class ConcurrentQueueStatisticsCollector : StatisticsCollector {

    private val scenarioStats = ConcurrentLinkedQueue<Statistics>()
    @Volatile private var startTime: Instant? = null

    override fun start(startTime: Instant) {
        this.startTime = startTime
    }

    override fun gather(statistics: Statistics) {
        checkNotNull(startTime) { "Collector not started." }
        this.scenarioStats.add(statistics)
    }

    override fun finish(endTime: Instant): ScenarioSummary {
        val startTime = checkNotNull(startTime) { "Collector not started." }
        require(endTime.isAfter(startTime)) { "End time is not after start time." }
        val summary = ScenarioSummary(scenarioStats, startTime, endTime)
        this.startTime = null
        this.scenarioStats.clear()
        return summary
    }
}
