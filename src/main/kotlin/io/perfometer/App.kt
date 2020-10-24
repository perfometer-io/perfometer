package io.perfometer

import io.perfometer.dsl.scenario
import java.time.Duration

fun main() {

    scenario("http://perfometer.io") {
                // Provide your scenario here
    }.run(10, Duration.ofSeconds(10))
}
