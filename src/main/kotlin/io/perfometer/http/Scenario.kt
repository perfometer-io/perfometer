package io.perfometer.http

import io.perfometer.dsl.RequestBuilder
import java.time.Duration

data class Scenario(
        val steps: List<Step>,
)

sealed class Step
data class RequestStep(val request: RequestBuilder) : Step()
data class PauseStep(val duration: Duration) : Step()
