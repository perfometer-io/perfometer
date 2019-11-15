package io.perfometer.runner

import io.perfometer.http.Scenario

internal interface ScenarioRunner {

    fun run(scenario : Scenario, configuration : RunnerConfiguration = RunnerConfiguration())
}
