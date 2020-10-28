package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.stream.appendHTML
import java.io.BufferedWriter
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object StatisticsHtmlFileWriter : StatisticsFileWriter() {

    fun write(summary: ScenarioSummary) {
        val reportFile = createReportFile(summary.startTime.toZonedDateTimeUTC(), Output.HTML)
        BufferedWriter(FileWriter(reportFile)).use {
            it.appendHTML(
                prettyPrint = true,
                xhtmlCompatible = true
            ).h1 {
                text("Scenario report: [${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())}]")
                if (summary.totalSummary == null) {
                    h2 { text("NO REQUESTS RUN") }
                }
            }.flush()
        }
    }

}
