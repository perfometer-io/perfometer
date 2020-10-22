package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.statistics.ScenarioStatistics

interface ScenarioRunner {

    fun runUsers(userCount: Int, block: () -> Unit)

    fun runStep(step: HttpStep)

    fun statistics(): ScenarioStatistics
}
