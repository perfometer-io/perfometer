package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toReadableString
import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.SummaryData
import io.perfometer.statistics.consumer.Output.HTML
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.BufferedWriter
import java.io.FileWriter

internal object StatisticsHtmlFileWriter : StatisticsFileWriter() {

    fun write(summary: ScenarioSummary) {
        val startTimeUtc = summary.startTime.toZonedDateTimeUTC()
        val reportFile = createReportFile(startTimeUtc, HTML)
        val totalSummary = summary.totalSummary
        BufferedWriter(FileWriter(reportFile)).use {
            it.appendHTML(
                prettyPrint = true,
                xhtmlCompatible = true
            ).body {
                h1 {
                    text("Scenario report: [${dateTimeFormatter.format(startTimeUtc)}]")
                }
                if (totalSummary == null) {
                    h2 { text("No requests were run") }
                } else {
                    table {
                        printHeader()
                        printSummary(totalSummary)
                        summary.summaries.forEach { s -> printSummary(s) }
                    }
                }
                style {
                    unsafe {
                        raw(
                            """
                                table {
                                  border-collapse: collapse;
                                }
                                td {
                                  border-right: 1px dashed #999;
                                  padding: 2px;
                                }
                                tr {
                                  border-bottom: 1px dashed #999;
                                }
                                tr.header {
                                  font-weight: bold;
                                }
                                tr.summary:hover {
                                  background-color: #DCDCDC;
                                }
                                tr.summary:nth-child(2) {
                                  background-color: #D3D3D3;
                                }
                            """.trimIndent()
                        )
                    }
                }
            }.flush()
        }
    }

    private fun TABLE.printHeader() {
        tr(classes = "header") {
            arrayOf(
                "REQUEST",
                "COUNT",
                "FAILED COUNT",
                "FASTEST TIME",
                "AVERAGE TIME",
                "95th PERCENTILE",
                "96th PERCENTILE",
                "97th PERCENTILE",
                "98th PERCENTILE",
                "99th PERCENTILE",
                "SLOWEST TIME"
            ).map { header -> td { text(header) } }
        }
    }

    private fun TABLE.printSummary(summaryData: SummaryData) {
        tr(classes = "summary") {
            arrayOf(
                summaryData.name,
                summaryData.requestCount.toString(),
                summaryData.failedRequestCount.toString(),
                summaryData.fastestTime.toReadableString(),
                summaryData.averageTime.toReadableString(),
                summaryData.percentile95Time.toReadableString(),
                summaryData.percentile96Time.toReadableString(),
                summaryData.percentile97Time.toReadableString(),
                summaryData.percentile98Time.toReadableString(),
                summaryData.percentile99Time.toReadableString(),
                summaryData.slowestTime.toReadableString()
            ).map { summaryValue -> td { text(summaryValue) } }
        }
    }

}
