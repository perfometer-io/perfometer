package io.perfometer.runner

import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.http.client.HttpClientFactory
import io.perfometer.statistics.ConcurrentQueueStatisticsCollector
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.StatisticsCollector
import java.time.Instant

abstract class BaseScenarioRunner(
    protected val httpClientFactory: HttpClientFactory,
    val statisticsCollector: StatisticsCollector = ConcurrentQueueStatisticsCollector(),
) : ScenarioRunner {

    protected suspend fun executeHttp(httpClient: HttpClient, requestStep: RequestStep) {
        val startTime = Instant.now()
        val request = requestStep.request
        val response = httpClient.executeHttp(request)
        val endTime = Instant.now()
        request.consumer(response)
        statisticsCollector.gather(
            RequestStatistics(
                request.name,
                request.method,
                request.pathWithParams,
                startTime,
                endTime,
                response.status,
            )
        )
    }
}
