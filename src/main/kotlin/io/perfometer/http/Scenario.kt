package io.perfometer.http

import io.perfometer.dsl.RequestBuilder
import java.time.Duration

/**
 * @author Piotr Wolny
 */
data class Scenario(val steps: List<Step>)

sealed class Step
data class RequestStep(val request: RequestBuilder, val response: HttpResponse): Step()
data class PauseStep(val duration: Duration) : Step()
