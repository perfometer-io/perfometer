package io.perfometer.runner

import io.perfometer.http.PauseStep
import io.perfometer.http.RequestStep
import io.perfometer.http.Scenario
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
import java.util.concurrent.Executors

internal class DefaultScenarioRunner(private val httpClient : HttpClient,
                                     private val statisticsPrinter : StatisticsPrinter) : ScenarioRunner {
    private lateinit var scenarioStatistics : ScenarioStatistics

    override fun run(scenario : Scenario, configuration : RunnerConfiguration) {
        scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
        runScenario(scenario, configuration)
    }

    private fun runScenario(scenario : Scenario, configuration : RunnerConfiguration) {
        val scenarioExecutor = Executors.newFixedThreadPool(configuration.threadCount)
        CompletableFuture.allOf(
                *(0 until configuration.threadCount)
                        .map { CompletableFuture.runAsync(Runnable { handleSteps(scenario.steps) }, scenarioExecutor) }
                        .toTypedArray())
                .thenRun { statisticsPrinter.print(scenarioStatistics.finish()) }
                .join()
        scenarioExecutor.shutdown()
    }

    private fun handleSteps(steps : List<Step>) = steps.forEach {
        when (it) {
            is RequestStep -> executeHttp(it)
            is PauseStep   -> pauseFor(it.duration)
        }
    }

    private fun pauseFor(duration : Duration) {
        Thread.sleep(duration.toMillis())
        scenarioStatistics.gather(PauseStatistics(duration))
    }

    private fun executeHttp(requestStep : RequestStep) {
        val startTime = Instant.now()
        val httpStatus = httpClient.executeHttp(requestStep.request, requestStep.response)
        requestStep.response.status = httpStatus
        val timeElapsed = Duration.between(startTime, Instant.now())
        scenarioStatistics.gather(RequestStatistics(
                requestStep.request.method,
                requestStep.request.pathWithParams(),
                timeElapsed,
                httpStatus)
        )
    }
}
