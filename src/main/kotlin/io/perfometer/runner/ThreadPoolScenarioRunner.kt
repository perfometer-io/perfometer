package io.perfometer.runner

import io.perfometer.dsl.*
import io.perfometer.http.client.HttpClientFactory
import io.perfometer.internal.helper.decorateInterruptable
import io.perfometer.internal.helper.decorateSuspendingInterruptable
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.ScenarioSummary
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class ThreadPoolScenarioRunner(
    httpClientFactory: HttpClientFactory,
) : BaseScenarioRunner(httpClientFactory) {

    private val httpClient = ThreadLocal.withInitial { httpClientFactory() }

    private val parallelJobs = ThreadLocal.withInitial { LinkedList<CompletableFuture<Void>>() }
    private val parallelJobsExecutor: ExecutorService = Executors.newCachedThreadPool()

    override fun runUsers(
        userCount: Int,
        duration: Duration,
        action: suspend () -> Unit,
    ): ScenarioSummary {
        statisticsCollector.start(Instant.now())
        return Executors.newFixedThreadPool(userCount).let { executor ->
            runUsersInternal(userCount, executor, action).let { usersFuture ->
                timeoutExecutors(duration, executor)
                usersFuture.join()
                statisticsCollector.finish(Instant.now())
            }
        }
    }

    override suspend fun runStep(step: HttpStep) {
        when (step) {
            is RequestStep -> executeHttp(httpClient.get(), step)
            is PauseStep -> pauseFor(step.duration)
            is ParallelStep -> runParallel(step)
        }
    }

    override suspend fun registerAsync(step: HttpStep) {
        parallelJobs.get().add(
            CompletableFuture.runAsync(
                { decorateInterruptable { runBlocking { runStep(step) } } }, parallelJobsExecutor
            )
        )
    }

    private fun runUsersInternal(
        userCount: Int,
        scenarioExecutor: ExecutorService,
        action: suspend () -> Unit,
    ): CompletableFuture<Void> {
        return CompletableFuture.allOf(
            *(0 until userCount)
                .map { CompletableFuture.runAsync({ runAction(action) }, scenarioExecutor) }
                .toTypedArray())
    }

    private fun timeoutExecutors(
        duration: Duration,
        scenarioExecutor: ExecutorService,
    ) {
        Executors.newSingleThreadScheduledExecutor().schedule({
            scenarioExecutor.shutdownNow()
            parallelJobsExecutor.shutdownNow()
        }, duration.toNanos(), TimeUnit.NANOSECONDS)
    }

    private fun runParallel(step: ParallelStep) {
        runBlocking {
            step.builder(HttpDsl(step.baseURL) { registerAsync(it) })
        }
        CompletableFuture.allOf(*parallelJobs.get().toTypedArray()).join()
    }

    private fun runAction(action: suspend () -> Unit) = decorateInterruptable {
        httpClient.set(httpClientFactory())
        runBlocking {
            while (!Thread.currentThread().isInterrupted) {
                decorateSuspendingInterruptable(action)
            }
        }
    }

    private fun pauseFor(duration: Duration) = decorateInterruptable {
        Thread.sleep(duration.toMillis())
        statisticsCollector.gather(PauseStatistics(duration))
    }

}
