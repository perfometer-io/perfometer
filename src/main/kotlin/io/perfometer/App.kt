package io.perfometer

import io.perfometer.dsl.scenario
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.printer.StdOutStatisticsPrinter
import io.perfometer.runner.DefaultScenarioRunner

fun main() {

    DefaultScenarioRunner(SimpleHttpClient(), StdOutStatisticsPrinter())
            .run(scenario("www.example.com", 443) {
                // Provide your scenario here
            })
}
