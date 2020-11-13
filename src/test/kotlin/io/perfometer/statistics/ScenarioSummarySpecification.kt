package io.perfometer.statistics

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import io.perfometer.statistics.StatisticsFixture.singleGetRequestStatistics
import java.time.Duration
import java.time.Instant
import kotlin.test.Test

class ScenarioSummarySpecification {
    private val start = Instant.ofEpochMilli(100)
    private val end = Instant.ofEpochSecond(10)

    private val ok = HttpStatus(200)

    @Test
    fun `should return total and failed request count`() {
        val fastest = singleGetRequestStatistics(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2))
        val slowest = singleGetRequestStatistics(Instant.ofEpochSecond(3), Instant.ofEpochSecond(6), HttpStatus(500))
        val average = singleGetRequestStatistics(Instant.ofEpochSecond(7), Instant.ofEpochSecond(9))
        val stats = setOf(fastest, slowest, average)

        val totalSummary = ScenarioSummary(stats, start, end).totalSummary
        totalSummary.shouldNotBeNull()
                .requestCount shouldBe 3
        totalSummary.failedRequestCount shouldBe 1
    }

    @Test
    fun `should return the slowest time`() {
        val fastest = singleGetRequestStatistics(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2))
        val slowest = singleGetRequestStatistics(Instant.ofEpochSecond(3), Instant.ofEpochSecond(6))
        val average = singleGetRequestStatistics(Instant.ofEpochSecond(7), Instant.ofEpochSecond(9))
        val stats = setOf(fastest, slowest, average)

        ScenarioSummary(stats, start, end).totalSummary.shouldNotBeNull()
                .slowestTime shouldBe slowest.timeTaken
    }

    @Test
    fun `should return the fastest time`() {
        val fastest = singleGetRequestStatistics(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2))
        val slowest = singleGetRequestStatistics(Instant.ofEpochSecond(3), Instant.ofEpochSecond(6))
        val average = singleGetRequestStatistics(Instant.ofEpochSecond(7), Instant.ofEpochSecond(9))
        val stats = setOf(fastest, slowest, average)

        ScenarioSummary(stats, start, end).totalSummary.shouldNotBeNull()
                .fastestTime shouldBe fastest.timeTaken
    }

    @Test
    fun `should return mean average request time`() {
        // given three request taking 1500 ms
        val fastest = singleGetRequestStatistics(Instant.ofEpochMilli(100), Instant.ofEpochMilli(200))
        val slowest = singleGetRequestStatistics(Instant.ofEpochMilli(200), Instant.ofEpochMilli(1200))
        val somewhereInBetween = singleGetRequestStatistics(Instant.ofEpochMilli(1200), Instant.ofEpochMilli(1600))

        val stats = setOf(fastest, slowest, somewhereInBetween)
        ScenarioSummary(stats, start, end).totalSummary.shouldNotBeNull()
                .averageTime shouldBe Duration.ofMillis(500)

        // given three requests summing up to one second
        val hundred = singleGetRequestStatistics(Instant.ofEpochMilli(100), Instant.ofEpochMilli(200))
        val fiveHundred = singleGetRequestStatistics(Instant.ofEpochMilli(200), Instant.ofEpochMilli(700))
        val fourHundred = singleGetRequestStatistics(Instant.ofEpochMilli(700), Instant.ofEpochMilli(1100))

        val oneSecondStats = setOf(hundred, fiveHundred, fourHundred)

        // Should truncate mean average duration to millis
        ScenarioSummary(oneSecondStats, start, end).totalSummary.shouldNotBeNull()
                .averageTime shouldBe Duration.ofMillis(333)
    }

    @Test
    fun `should return request times greater or equal than 9Xth percentile of requests`() {
        val stats = (1..100).map {
            singleGetRequestStatistics(Instant.ofEpochMilli(it.toLong()), Instant.ofEpochMilli((2 * it).toLong()))
        }

        val totalSummary = ScenarioSummary(stats, start, end).totalSummary
        totalSummary
            .shouldNotBeNull()
            .percentile95Time shouldBe Duration.ofMillis(95)
        totalSummary.percentile96Time shouldBe Duration.ofMillis(96)
        totalSummary.percentile97Time shouldBe Duration.ofMillis(97)
        totalSummary.percentile98Time shouldBe Duration.ofMillis(98)
        totalSummary.percentile99Time shouldBe Duration.ofMillis(99)
    }

    @Test
    fun `should calculate requests per second`() {
        val stats = (0 until 1000).map {
            singleGetRequestStatistics(Instant.ofEpochMilli(10 * it.toLong()), Instant.ofEpochMilli((10 * it).toLong()))
        }.union((1..200).map {
            singleGetRequestStatistics(Instant.ofEpochMilli(10000 + it.toLong()), Instant.ofEpochMilli(10000 + it.toLong()))
        })

        val totalSummary = ScenarioSummary(stats, start, end).totalSummary
        totalSummary
            .shouldNotBeNull()
            .rps shouldBe listOf(100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 200)
        totalSummary.minimumRps shouldBe 100
        totalSummary.averageRps shouldBe 109
        totalSummary.maximumRps shouldBe 200
    }

    @Test
    fun `should return null summary if no requests in statistics`() {
        ScenarioSummary(emptyList(), start, end).totalSummary.shouldBeNull()
    }

    @Test
    fun `should return scenario running time`() {
        val summary = ScenarioSummary(setOf(singleGetRequestStatistics(Instant.ofEpochMilli(100), Instant.ofEpochMilli(200))),
                start,
                end)
        summary.scenarioDuration shouldBe Duration.between(start, end)
    }

    @Test
    fun `should group requests by name`() {
        val fastest = RequestStatistics("name1", HttpMethod.GET, "", Instant.ofEpochSecond(1), Instant.ofEpochSecond(2), ok)
        val slowest = RequestStatistics("name2", HttpMethod.GET, "", Instant.ofEpochSecond(3), Instant.ofEpochSecond(6), ok)
        val average = RequestStatistics("name1", HttpMethod.GET, "", Instant.ofEpochSecond(7), Instant.ofEpochSecond(9), ok)
        val stats = setOf(fastest, slowest, average)

        val summaries = ScenarioSummary(stats, start, end).summaries
        summaries shouldHaveSize 2
        summaries shouldContain SummaryData(
            "name1",
            listOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0),
            2,
            0,
            Duration.ofSeconds(1),
            Duration.ofMillis(1500),
            Duration.ofSeconds(2),
            Duration.ofSeconds(2),
            Duration.ofSeconds(2),
            Duration.ofSeconds(2),
            Duration.ofSeconds(2),
            Duration.ofSeconds(2)
        )
        summaries shouldContain SummaryData(
            "name2",
            listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
            1,
            0,
            Duration.ofSeconds(3),
            Duration.ofSeconds(3),
            Duration.ofSeconds(3),
            Duration.ofSeconds(3),
            Duration.ofSeconds(3),
            Duration.ofSeconds(3),
            Duration.ofSeconds(3),
            Duration.ofSeconds(3)
        )
    }
}
