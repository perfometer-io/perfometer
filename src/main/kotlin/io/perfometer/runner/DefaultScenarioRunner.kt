package io.perfometer.runner

import io.perfometer.dsl.ScenarioBuilder
import io.perfometer.http.*
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.*
import io.perfometer.statistics.printer.StatisticsPrinter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.*

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
        val scenarioExecutor = Executors.newFixedThreadPool(configuration.threads)
        CompletableFuture.allOf(
                *(0 until configuration.threads)
                        .map { CompletableFuture.runAsync({ handleSteps(scenario.build().steps) }, scenarioExecutor) }
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
