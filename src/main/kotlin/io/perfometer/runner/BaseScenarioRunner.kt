package io.perfometer.runner

import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioStatistics
import java.time.Duration
import java.time.Instant

abstract class BaseScenarioRunner(
        private val httpClient: HttpClient,
        override val statistics: ScenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now()),
) : ScenarioRunner {

    protected fun executeHttp(requestStep: RequestStep) {
        val startTime = Instant.now()
        val request = requestStep.request
        val response = httpClient.executeHttp(request)
        val timeElapsed = Duration.between(startTime, Instant.now())
        request.consumer(response)
        statistics.gather(RequestStatistics(
                request.method,
                request.pathWithParams,
                timeElapsed,
                response.status)
        )
    }
}
