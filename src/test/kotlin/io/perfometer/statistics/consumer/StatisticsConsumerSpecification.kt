package io.perfometer.statistics.consumer

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.RequestStatistics
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class StatisticsConsumerSpecification {

    private val reportFilePath = Paths.get("${System.getProperty("user.dir")}/reports/report.txt")

    @BeforeTest
    fun setup() {
        if (Files.exists(reportFilePath)) {
            Files.delete(reportFilePath)
        }
    }

    @Test
    fun `should create a report file`() {
        // given a statistics with summary
        val scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
        scenarioStatistics.gather(RequestStatistics("name", HttpMethod.GET, "/", Duration.ofSeconds(1), HttpStatus(200)))

        // when consuming the stats with Output.TEXT_FILE option selected
        consumeStatistics(scenarioStatistics.finish(), Output.TEXT_FILE)

        // then a text file should be created
        assertTrue { Files.exists(reportFilePath) }
    }
}
