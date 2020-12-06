package io.perfometer.statistics.consumer

import io.perfometer.internal.helper.toReadableString
import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.SummaryData
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.BufferedWriter
import java.io.StringWriter

internal object StatisticsFormatter {

    fun formatAsText(summary: ScenarioSummary): String {
        return if (summary.totalSummary == null) {
            "===> No requests run! <==="
        } else {
            """Scenario Duration: ${summary.scenarioDuration.toReadableString()}
                +${printHeader()}
                +${printSummary(summary.totalSummary)}
                +${printRequests(summary)}
            """.trimMargin(marginPrefix = "+")
        }
    }

    fun formatAsHtml(summary: ScenarioSummary): String {
        val startTimeUtc = summary.startTime.toZonedDateTimeUTC()
        val totalSummary = summary.totalSummary

        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use {
            it.appendHTML(
                prettyPrint = true,
                xhtmlCompatible = true
            ).body {
                h1 {
                    text(
                        "Scenario report: [${
                            StatisticsFileWriter.dateTimeFormatter.format(
                                startTimeUtc
                            )
                        }]"
                    )
                }
                if (totalSummary == null) {
                    h2 { text("No requests were run") }
                } else {
                    table {
                        printHeader()
                        printSummary(totalSummary)
                        printRequests(summary.summaries)
                    }
                }
                printCss()
            }.flush()
        }
        return String(stringWriter.buffer)
    }

    private fun printHeader(): String {
        return SummaryData.headerNames.joinToString(
            separator = "",
            postfix = " |"
        ) { printColumn(it) }
    }

    private fun printSummary(sd: SummaryData): String {
        return sd.printableValues.joinToString(separator = "", postfix = " |") { printColumn(it) }
    }

    private fun printRequests(summary: ScenarioSummary): String {
        return summary.summaries.joinToString(separator = "\n") { printSummary(it) }
    }

    private fun printColumn(value: String): String {
        return "| ${value.padEnd(15, ' ').substring(0, 15)} "
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

    private fun TABLE.printRequests(summaries: List<SummaryData>) {
        summaries.forEach { s -> printSummary(s) }
    }

    private fun BODY.printCss() {
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
                      """
                )
            }
        }
    }

}
