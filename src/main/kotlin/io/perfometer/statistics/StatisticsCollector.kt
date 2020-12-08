package io.perfometer.statistics

import java.time.Instant

interface StatisticsCollector {

    fun start(startTime: Instant)

    fun gather(statistics: Statistics)

    fun finish(endTime: Instant): ScenarioSummary
}
