package io.perfometer

import io.perfometer.dsl.scenario

fun main() {

    scenario("http://perfometer.io") {
                // Provide your scenario here
    }.run(10)
}
