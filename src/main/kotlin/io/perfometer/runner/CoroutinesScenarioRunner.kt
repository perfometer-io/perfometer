package io.perfometer.runner

import io.perfometer.dsl.HttpStep
import io.perfometer.dsl.ParallelStep
import io.perfometer.dsl.PauseStep
import io.perfometer.dsl.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.http.client.HttpClientFactory
import io.perfometer.statistics.PauseStatistics
import io.perfometer.statistics.ScenarioSummary
import kotlinx.coroutines.*
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal class CoroutinesScenarioRunner(
    httpClientFactory: HttpClientFactory,
) : BaseScenarioRunner(httpClientFactory) {

    override fun runUsers(
        userCount: Int,
        duration: Duration,
        action: suspend () -> Unit,
    ): ScenarioSummary {
        runBlocking(Dispatchers.Default) {
            withTimeoutOrNull(duration.toMillis()) {
                (1..userCount).map {
                    launch(CoroutineJobContext(httpClientFactory())) {
                        while (isActive) action()
                    }
                }
            }
        }
        return statistics.finish()
    }

    override suspend fun runStep(step: HttpStep) {
        when (step) {
            is RequestStep -> executeHttp(
                context().httpClient,
                step,
            )
            is PauseStep -> pauseFor(step.duration)
            is ParallelStep -> runParallel(step)
        }
    }

    /**
     * Registers async step at the level of a 'user-coroutine'.
     * Parallel blocks run inside one 'user-coroutine' share the same queue of async tasks.
     */
    override suspend fun registerAsync(step: HttpStep) = coroutineScope<Unit> {
        context().asyncSteps.add(step)
    }

    /**
     * Registers steps for async execution, calling step's asyncRegistrator function,
     * and then iterates over registered tasks creating a separate coroutine for each.
     *
     * This method will not return until either all child coroutines are Completed, or terminate with an exception,
     * or the parent 'user-coroutines' times out, effectively cancelling all pending jobs.
     */
    private suspend fun runParallel(step: ParallelStep) =
        coroutineScope {
            step.asyncRegistrator()
            while (hasNextJob()) {
                runJob(this@CoroutinesScenarioRunner, this)
            }
        }

    private suspend fun runJob(
        coroutinesScenarioRunner: CoroutinesScenarioRunner,
        coroutineScope: CoroutineScope
    ) {
        val nextStep = coroutinesScenarioRunner.context().asyncSteps.poll()
        coroutineScope.launch(CoroutineJobContext(httpClientFactory())) {
            runStep(nextStep)
        }
    }

    private suspend fun hasNextJob() = context().asyncSteps.size > 0

    private suspend fun pauseFor(duration: Duration) {
        delay(duration.toMillis())
        statistics.gather(PauseStatistics(duration))
    }

    private suspend fun context(): CoroutineJobContext {
        return coroutineContext[CoroutineJobContext.Key]
            ?: throw IllegalStateException("Required context not setup")
    }

    private data class CoroutineJobContext(
        val httpClient: HttpClient,
        val asyncSteps: Deque<HttpStep> = ConcurrentLinkedDeque(),
    ) : AbstractCoroutineContextElement(CoroutineJobContext) {
        companion object Key : CoroutineContext.Key<CoroutineJobContext>
    }
}
