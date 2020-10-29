package io.perfometer.statistics.consumer

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
            SummaryData.mapHeaders { header -> td { text(header) } }
        }
    }

    private fun TABLE.printSummary(summaryData: SummaryData) {
        tr(classes = "summary") {
            summaryData.mapPrintableValues { pv -> td { text(pv) } }
        }
    }

}
