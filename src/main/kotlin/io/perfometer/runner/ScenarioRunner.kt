package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.statistics.ScenarioStatistics

interface ScenarioRunner {

    val statistics: ScenarioStatistics

    fun runUsers(userCount: Int, action: suspend () -> Unit)

    suspend fun runStep(step: HttpStep)
}
