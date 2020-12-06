package io.perfometer.statistics.consumer

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import io.perfometer.internal.helper.toReadableString
import io.perfometer.internal.helper.toZonedDateTimeUTC
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.SummaryData
import java.io.File


internal object StatisticsPdfFileWriter : StatisticsFileWriter() {

    private const val LEFT = 10f
    private const val RIGHT = 10f
    private const val TOP = 10f
    private const val BOTTOM = 10f

    private const val TITLE_FONT_SIZE = 12f
    private const val HEADER_FONT_SIZE = 8f
    private const val CELL_FONT_SIZE = 8f


    private val titleFont = FontFactory.getFont(
        FontFactory.HELVETICA_BOLD, TITLE_FONT_SIZE, BaseColor.BLACK
    )

    private val headerFont = FontFactory.getFont(
        FontFactory.HELVETICA_BOLD, HEADER_FONT_SIZE, BaseColor.BLACK
    )

    private val cellFont = FontFactory.getFont(
        FontFactory.HELVETICA, CELL_FONT_SIZE, BaseColor.BLACK
    )

    fun write(summary: ScenarioSummary) {
        val startTimeUtc = summary.startTime.toZonedDateTimeUTC()
        val reportFile = createReportFile(startTimeUtc, Output.PDF)
        writePdf(reportFile, summary)
    }

    private fun writePdf(reportFile: File, summary: ScenarioSummary) {
        Document(PageSize.A4.rotate(), LEFT, RIGHT, TOP, BOTTOM).also { document ->
            PdfWriter.getInstance(document, reportFile.outputStream())
            document.open()
            writeDoc(summary, document)
            document.close()
        }
    }

    private fun writeDoc(summary: ScenarioSummary, document: Document) {
        PdfPTable(1).also { layoutTable ->
            setupTable(layoutTable)
            writeTitle(layoutTable, summary)
            if (summary.totalSummary == null) {
                writeNoRequests(layoutTable)
            } else {
                PdfPTable(SummaryData.headerNames.size).also { summaryTable ->
                    setupTable(summaryTable)
                    writeHeader(summaryTable)
                    writeTotalSummary(summary.totalSummary, summaryTable)
                    writeRequests(summary, summaryTable)
                    layoutTable.addCell(summaryTable)
                }
            }
            document.add(layoutTable)
        }

    }

    private fun writeNoRequests(layoutTable: PdfPTable) {
        layoutTable.addCell("No requests run")
    }

    private fun writeTitle(
        layoutTable: PdfPTable,
        summary: ScenarioSummary
    ) {
        val startTimeUtc = summary.startTime.toZonedDateTimeUTC()
        layoutTable.addCell(
            Paragraph(
                "Scenario report: [${dateTimeFormatter.format(startTimeUtc)}]",
                titleFont
            )
        )
        layoutTable.addCell(
            Paragraph(
                "Scenario duration: [${summary.scenarioDuration.toReadableString()}]",
                headerFont
            )
        )
    }

    private fun setupTable(table: PdfPTable) {
        table.widthPercentage = 100f
    }

    private fun writeHeader(table: PdfPTable) {
        SummaryData.headerNames.forEach { name ->
            with(PdfPCell()) {
                borderWidth = 2f
                phrase = Phrase(name, headerFont)
                table.addCell(this)
            }
        }
    }

    private fun writeTotalSummary(
        data: SummaryData,
        table: PdfPTable
    ) {
        data.printableValues.forEach { writeCell(it, table) }
    }

    private fun writeRequests(
        summary: ScenarioSummary,
        table: PdfPTable
    ) {
        summary.summaries.forEach { summaryData ->
            summaryData.printableValues.forEach { writeCell(it, table) }
        }
    }

    private fun writeCell(
        content: String,
        table: PdfPTable
    ) {
        val phrase = Phrase(content, cellFont)
        table.addCell(phrase)
    }
}
