package io.perfometer

import io.perfometer.dsl.scenario
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.runner.DefaultScenarioRunner
import io.perfometer.runner.RunnerConfiguration
import io.perfometer.statistics.printer.StdOutStatisticsPrinter

fun main() {

    DefaultScenarioRunner(SimpleHttpClient(true), StdOutStatisticsPrinter())
            .run(scenario("http://perfometer.io") {
                // Provide your scenario here
            }, RunnerConfiguration(10))
}
