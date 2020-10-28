package io.perfometer.statistics.consumer

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ConcurrentQueueScenarioStatistics
import io.perfometer.statistics.RequestStatistics
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.consumer.FileTypeExtension.TXT
import io.perfometer.statistics.consumer.StatisticsFileWriter.Companion.dateTimeFormatter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class StatisticsConsumerSpecification {

    private val reportDirectoryPath = Paths.get("${System.getProperty("user.dir")}/reports")

    @BeforeTest
    fun setup() {
        removeReportsFiles()
    }

    @AfterTest
    fun cleanup() {
        removeReportsFiles()
    }

    @Test
    fun `should create a report file`() {
        // given a statistics with summary
        val scenarioStatistics = ConcurrentQueueScenarioStatistics(Instant.now())
        scenarioStatistics.gather(
            RequestStatistics(
                "name",
                HttpMethod.GET,
                "/",
                Duration.ofSeconds(1),
                HttpStatus(200)
            )
        )

        // when consuming the stats with Output.TEXT_FILE option selected
        val scenarioSummary: ScenarioSummary = scenarioStatistics.finish()
        consumeStatistics(scenarioSummary, Output.TEXT_FILE)

        // then a text file should be created with scenario start time UTC timestamp in filename
        val timestamp = dateTimeFormatter.format(scenarioSummary.startTime.toZonedDateTimeUTC())
        val reportPath = reportDirectoryPath.resolve("report-${timestamp}${TXT.fileExtension}")
        assertTrue { Files.exists(reportPath) }
    }

    private fun removeReportsFiles() {
        if (Files.exists(reportDirectoryPath)) {
            val files = reportDirectoryPath.toFile().listFiles() ?: emptyArray()
            files.forEach { it.delete() }
        }
    }

}
