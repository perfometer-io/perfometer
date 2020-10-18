package io.perfometer.runner

import io.perfometer.dsl.ScenarioBuilder
import io.perfometer.http.PauseStep
import io.perfometer.http.RequestStep
import io.perfometer.http.Step
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioStatistics
import io.perfometer.statistics.printer.StatisticsPrinter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class DefaultScenarioRunner(
        private val httpClient: HttpClient,
        private val statisticsPrinter: StatisticsPrinter,
) : ScenarioRunner {
    private lateinit var scenarioStatistics: ScenarioStatistics

    override fun run(scenario: ScenarioBuilder, configuration: RunnerConfiguration) {
        scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
        runScenario(scenario, configuration)
    }

    private fun runScenario(scenario: ScenarioBuilder, configuration: RunnerConfiguration) {
        val scenarioExecutor = Executors.newFixedThreadPool(configuration.threadCount)
        CompletableFuture.allOf(
                *(0 until configuration.threadCount)
                        .map { CompletableFuture.runAsync(Runnable { handleSteps(scenario.build().steps) }, scenarioExecutor) }
                        .toTypedArray())
                .thenRun { statisticsPrinter.print(scenarioStatistics.finish()) }
                .join()
        shutdown(scenarioExecutor)
    }

    private fun shutdown(scenarioExecutor: ExecutorService) {
        scenarioExecutor.shutdown()
        val isTerminated = scenarioExecutor.awaitTermination(10, TimeUnit.SECONDS)
        if (!isTerminated) {
            scenarioExecutor.shutdownNow()
        }
    }

    private fun handleSteps(steps: List<Step>) = steps.forEach {
        when (it) {
            is RequestStep -> executeHttp(it)
            is PauseStep -> pauseFor(it.duration)
        }
    }

    private fun pauseFor(duration: Duration) {
        Thread.sleep(duration.toMillis())
        scenarioStatistics.gather(PauseStatistics(duration))
    }

    private fun executeHttp(requestStep: RequestStep) {
        val startTime = Instant.now()
        val request = requestStep.request
        val response = httpClient.executeHttp(request)
        val timeElapsed = Duration.between(startTime, Instant.now())
        request.consumer(response)
        scenarioStatistics.gather(RequestStatistics(
                request.method,
                request.pathWithParams(),
                timeElapsed,
                response.status)
        )
    }
}
