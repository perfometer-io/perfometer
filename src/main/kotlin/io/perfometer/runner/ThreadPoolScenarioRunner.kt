package io.perfometer.runner

import io.perfometer.dsl.PauseStep
import io.perfometer.dsl.RequestStep
import io.perfometer.dsl.HttpStep
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioStatistics
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class ThreadPoolScenarioRunner(
        private val httpClient: HttpClient,
        private val scenarioStatistics: ScenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
) : ScenarioRunner {

    override fun runUsers(userCount: Int, block: () -> Unit) {
        val scenarioExecutor = Executors.newFixedThreadPool(userCount)
        CompletableFuture.allOf(
                *(0 until userCount)
                        .map { CompletableFuture.runAsync(block, scenarioExecutor) }
                        .toTypedArray())
                .join()
        shutdown(scenarioExecutor)
    }

    override fun runStep(step: HttpStep) {
        when (step) {
            is RequestStep -> executeHttp(step)
            is PauseStep -> pauseFor(step.duration)
        }
    }

    override fun statistics(): ScenarioStatistics {
        return scenarioStatistics
    }

    private fun shutdown(scenarioExecutor: ExecutorService) {
        scenarioExecutor.shutdown()
        val isTerminated = scenarioExecutor.awaitTermination(10, TimeUnit.SECONDS)
        if (!isTerminated) {
            scenarioExecutor.shutdownNow()
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
                request.pathWithParams,
                timeElapsed,
                response.status)
        )
    }
}
