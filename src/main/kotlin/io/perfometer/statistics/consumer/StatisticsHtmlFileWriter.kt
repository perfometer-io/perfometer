package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.consumer.Output.HTML

internal object StatisticsHtmlFileWriter : StatisticsFileWriter() {

    fun write(summary: ScenarioSummary) {
        val startTimeUtc = summary.startTime.toZonedDateTimeUTC()
        val reportFile = createReportFile(startTimeUtc, HTML)
        reportFile.writeText(StatisticsFormatter.formatAsHtml(summary))
    }

}
