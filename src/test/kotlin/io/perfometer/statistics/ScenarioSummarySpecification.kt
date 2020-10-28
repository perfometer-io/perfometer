package io.perfometer.statistics

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class ScenarioSummarySpecification {
    private val start = Instant.MIN
    private val end = Instant.MAX

    private val ok = HttpStatus(200)

    @Test
    fun `should return total and failed request count`() {
        val fastest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(1), ok)
        val slowest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(3), HttpStatus(500))
        val average = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(2), ok)
        val stats = setOf(fastest, slowest, average)

        val totalSummary = ScenarioSummary(stats, start, end).totalSummary
        totalSummary.shouldNotBeNull()
                .requestCount shouldBe 3
        totalSummary.failedRequestCount shouldBe 1
    }

    @Test
    fun `should return the slowest time`() {
        val fastest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(1), ok)
        val slowest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(3), ok)
        val average = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(2), ok)
        val stats = setOf(fastest, slowest, average)

        ScenarioSummary(stats, start, end).totalSummary.shouldNotBeNull()
                .slowestTime shouldBe slowest.timeTaken
    }

    @Test
    fun `should return the fastest time`() {
        val fastest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(1), ok)
        val slowest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(3), ok)
        val average = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofSeconds(2), ok)
        val stats = setOf(fastest, slowest, average)

        ScenarioSummary(stats, start, end).totalSummary.shouldNotBeNull()
                .fastestTime shouldBe fastest.timeTaken
    }

    @Test
    fun `should return mean average request time`() {
        // given three request taking 1500 ms
        val fastest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(100), ok)
        val slowest = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(1000), ok)
        val somewhereInBetween = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(400), ok)

        val stats = setOf(fastest, slowest, somewhereInBetween)
        ScenarioSummary(stats, start, end).totalSummary.shouldNotBeNull()
                .averageTime shouldBe Duration.ofMillis(500)

        // given three requests summing up to one second
        val hundred = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(100), ok)
        val fiveHundred = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(500), ok)
        val fourHundred = RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(400), ok)

        val oneSecondStats = setOf(hundred, fiveHundred, fourHundred)

        // Should truncate mean average duration to millis
        ScenarioSummary(oneSecondStats, start, end).totalSummary.shouldNotBeNull()
                .averageTime shouldBe Duration.ofMillis(333)
    }

    @Test
    fun `should return request times greater or equal than 9Xth percentile of requests`() {
        val stats = (1..100).map {
            RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(it.toLong()), ok)
        }

        val totalSummary = ScenarioSummary(stats, start, end).totalSummary
        totalSummary.shouldNotBeNull()
                .percentile95Time shouldBe Duration.ofMillis(95)
        totalSummary.percentile96Time shouldBe Duration.ofMillis(96)
        totalSummary.percentile97Time shouldBe Duration.ofMillis(97)
        totalSummary.percentile98Time shouldBe Duration.ofMillis(98)
        totalSummary.percentile99Time shouldBe Duration.ofMillis(99)
    }

    @Test
    fun `should return null summary if no requests in statistics`() {
        ScenarioSummary(emptyList(), start, end).totalSummary.shouldBeNull()
    }

    @Test
    fun `should return scenario running time`() {
        val summary = ScenarioSummary(setOf(RequestStatistics("GET", HttpMethod.GET, "", Duration.ofMillis(100), ok)),
                start,
                end)
        summary.scenarioDuration shouldBe Duration.between(start, end)
    }

    @Test
    fun `should group requests by name`() {
        val fastest = RequestStatistics("name1", HttpMethod.GET, "", Duration.ofSeconds(1), ok)
        val slowest = RequestStatistics("name2", HttpMethod.GET, "", Duration.ofSeconds(3), ok)
        val average = RequestStatistics("name1", HttpMethod.GET, "", Duration.ofSeconds(2), ok)
        val stats = setOf(fastest, slowest, average)

        val summaries = ScenarioSummary(stats, start, end).summaries
        summaries shouldHaveSize 2
        summaries shouldContain SummaryData("name1",
                                            2,
                                            0,
                                            Duration.ofSeconds(1),
                                            Duration.ofMillis(1500),
                                            Duration.ofSeconds(2),
                                            Duration.ofSeconds(2),
                                            Duration.ofSeconds(2),
                                            Duration.ofSeconds(2),
                                            Duration.ofSeconds(2),
                                            Duration.ofSeconds(2))
        summaries shouldContain SummaryData("name2",
                                            1,
                                            0,
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3),
                                            Duration.ofSeconds(3))
    }
}
