package io.perfometer.statistics

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import io.perfometer.http.Get
import io.perfometer.http.HttpStatus
import io.perfometer.http.Post
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

@Suppress("FunctionName")
class ConcurrentQueueScenarioStatisticsSpecification {

    private val scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())

    @Test
    fun `gather should add statistics to the summary`() {
        val getRequest = Get("perfometer.io", 443, "/")
        val getStatistics = RequestStatistics(getRequest, Duration.ofSeconds(1), HttpStatus(200))
        scenarioStatistics.gather(getStatistics)

        val postRequest = Post("perfometer.io", 443, "/post")
        val postStatistics = RequestStatistics(postRequest, Duration.ofSeconds(1), HttpStatus(201))
        scenarioStatistics.gather(postStatistics)

        val scenarioSummary = scenarioStatistics.getSummary()
        scenarioSummary.statistics.size shouldBe 2
        scenarioSummary.statistics shouldContain getStatistics
        scenarioSummary.statistics shouldContain postStatistics
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `statistics should return a defensive, immutable copy of the underlying collection`() {
        val getRequest = Get("perfometer.io", 443, "/")
        val statistics = RequestStatistics(getRequest, Duration.ofSeconds(1), HttpStatus(200))
        scenarioStatistics.gather(statistics)

        val scenarioSummary = scenarioStatistics.getSummary()
        scenarioSummary.statistics.size shouldBe 1
        (scenarioSummary.statistics as MutableList).add(statistics)
    }
}
