package io.perfometer.runner

import io.perfometer.http.*
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioStatistics
import io.perfometer.statistics.printer.StatisticsPrinter
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
                        .map { CompletableFuture.runAsync { handleSteps(scenario.steps) } }
                        .toTypedArray())
                .thenRun { scenarioStatistics.endTime = Instant.now() }
                .thenRun { statisticsPrinter.print(scenarioStatistics.getSummary()) }
                .join()
    }

    private fun handleSteps(steps: List<Step>) = steps.forEach {
        when (it) {
            is RequestStep -> executeHttp(it.request)
            is PauseStep -> pauseFor(it.duration)
        }
    }

    private fun pauseFor(duration: Duration) {
        Thread.sleep(duration.toMillis())
        scenarioStatistics.gather(PauseStatistics(duration))
    }

    private fun executeHttp(request: HttpRequest) {
        val startTime = Instant.now()
        val httpStatus = httpClient.executeHttp(request)
        val timeElapsed = Duration.between(startTime, Instant.now())
        scenarioStatistics.gather(RequestStatistics(request, timeElapsed, httpStatus))
    }
}
