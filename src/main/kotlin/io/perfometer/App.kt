package io.perfometer

import io.perfometer.dsl.scenario
import io.perfometer.statistics.consumer.Output
import java.time.Duration

fun main() {

    scenario("http://example.com") {
        // Provide your scenario here
    }.run(
        userCount = 10,
        duration = Duration.ofSeconds(10),
        outputTo = arrayOf(Output.STDOUT)
    )

}
