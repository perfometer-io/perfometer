package io.perfometer

import io.perfometer.dsl.scenario
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.runner.DefaultScenarioRunner
import io.perfometer.statistics.printer.StdOutStatisticsPrinter

fun main() {

    DefaultScenarioRunner(SimpleHttpClient(true), StdOutStatisticsPrinter())
            .run(scenario("https","www.example.com", 443) {
                // Provide your scenario here
            })
}
