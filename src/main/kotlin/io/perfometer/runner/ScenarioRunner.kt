package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.statistics.ScenarioStatistics
import java.time.Duration

interface ScenarioRunner {

    val statistics: ScenarioStatistics

    fun runUsers(userCount: Int, duration: Duration, action: suspend () -> Unit)

    suspend fun runStep(step: HttpStep)
}
