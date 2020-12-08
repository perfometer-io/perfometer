package io.perfometer.statistics

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Instant
import kotlin.test.Test

@Suppress("FunctionName")
class ConcurrentQueueStatisticsCollectorSpecification {

    @Test
    fun `gather should add statistics to the summary`() {
        val scenarioStatistics = ConcurrentQueueStatisticsCollector()
        scenarioStatistics.start(Instant.ofEpochSecond(0))

        scenarioStatistics.gather(StatisticsFixture.singleGetRequestStatistics())

        val postStatistics = RequestStatistics("POST /", HttpMethod.POST, "/", Instant.ofEpochSecond(3), Instant.ofEpochSecond(4), HttpStatus(201))
        scenarioStatistics.gather(postStatistics)

        val scenarioSummary = scenarioStatistics.finish(Instant.ofEpochSecond(5))
        scenarioSummary.totalSummary.shouldNotBeNull()
                .requestCount shouldBe 2
        scenarioSummary.summaries.size shouldBe 2
    }

    @Test(IllegalStateException::class)
    fun `gather should not allow adding new statistics when the scenario was not started`() {
        val scenarioStatistics = ConcurrentQueueStatisticsCollector()
        scenarioStatistics.gather(StatisticsFixture.singleGetRequestStatistics())
    }

    @Test(IllegalStateException::class)
    fun `gather should not allow adding new statistics when the scenario has ended`() {
        val scenarioStatistics = ConcurrentQueueStatisticsCollector()
        scenarioStatistics.start(Instant.ofEpochSecond(0))
        scenarioStatistics.gather(StatisticsFixture.singleGetRequestStatistics())
        scenarioStatistics.finish(Instant.ofEpochSecond(5))

        scenarioStatistics.gather(StatisticsFixture.singleGetRequestStatistics())
    }

    @Test(IllegalArgumentException::class)
    fun `gather should not allow finish with time before start time`() {
        val scenarioStatistics = ConcurrentQueueStatisticsCollector()

        scenarioStatistics.start(Instant.ofEpochSecond(5))
        scenarioStatistics.gather(
            StatisticsFixture.singleGetRequestStatistics(
                Instant.ofEpochSecond(6),
                Instant.ofEpochSecond(7),
            )
        )
        scenarioStatistics.finish(Instant.ofEpochSecond(1))
    }

}
