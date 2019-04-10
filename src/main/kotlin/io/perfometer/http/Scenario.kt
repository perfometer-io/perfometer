package io.perfometer.http

import java.time.Duration

/**
 * @author Piotr Wolny
 */
data class Scenario(val steps: List<Step>)

sealed class Step
data class RequestStep(val request: HttpRequest) : Step()
data class PauseStep(val duration: Duration) : Step()
