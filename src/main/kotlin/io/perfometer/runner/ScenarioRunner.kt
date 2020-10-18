package io.perfometer.runner

import io.perfometer.dsl.ScenarioBuilder

internal interface ScenarioRunner {

    fun run(scenario: ScenarioBuilder, configuration: RunnerConfiguration = RunnerConfiguration())
}
