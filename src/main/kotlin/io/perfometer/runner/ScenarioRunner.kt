package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.statistics.ScenarioSummary
import java.time.Duration

interface ScenarioRunner {

    fun runUsers(userCount: Int, duration: Duration, action: suspend () -> Unit): ScenarioSummary

    suspend fun runStep(step: HttpStep)

    suspend fun runStepAsync(step: HttpStep)
}
