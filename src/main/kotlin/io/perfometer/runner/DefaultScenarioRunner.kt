package io.perfometer.runner

import io.perfometer.http.*
import io.perfometer.http.client.HttpClient
import io.perfometer.http.client.httpConnection
import io.perfometer.statistics.printer.StatisticsPrinter
import io.perfometer.statistics.*
import java.net.HttpURLConnection
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture

/**
 * @author Tomasz Tarczyński
 */
internal class DefaultScenarioRunner(private val httpClient: HttpClient,
                                     private val statisticsPrinter: StatisticsPrinter) : ScenarioRunner {
    private lateinit var scenarioStatistics: ScenarioStatistics

    override fun run(scenario: Scenario, configuration: RunnerConfiguration) {
        scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
        runThreads(configuration, scenario)
    }

    private fun runThreads(configuration: RunnerConfiguration, scenario: Scenario) {
        CompletableFuture.allOf(
                *(0 until configuration.threadCount)
                        .map { CompletableFuture.runAsync { handleSteps(configuration, scenario.steps) } }
                        .toTypedArray())
                .thenRun { scenarioStatistics.endTime = Instant.now() }
                .thenRun { statisticsPrinter.print(scenarioStatistics.getSummary()) }
                .join()
    }

    private fun handleSteps(configuration: RunnerConfiguration, steps: List<Step>) = steps.forEach {
        when (it) {
            is RequestStep -> executeHttp(configuration, it.request)
            is PauseStep -> pauseFor(it.duration)
        }
    }

    private fun pauseFor(duration: Duration) {
        Thread.sleep(duration.toMillis())
        scenarioStatistics.gather(PauseStatistics(duration))
    }

    private fun executeHttp(configuration: RunnerConfiguration, request: HttpRequest) {
        val startTime = Instant.now()
        val httpStatus = httpClient.executeHttp(createHttpConnectionForRequest(configuration, request))
        val timeElapsed = Duration.between(startTime, Instant.now())
        scenarioStatistics.gather(RequestStatistics(request, timeElapsed, httpStatus))
    }

    private fun createHttpConnectionForRequest(configuration: RunnerConfiguration, request: HttpRequest): HttpURLConnection {
        return httpConnection("https", request.host, request.port, request.path) {
            if (configuration.trustAllCertificates) {
                trustAllCertificates()
            }
            method(request.name)
            headers(request.headers)
            body(request.body)
        }
    }
}
