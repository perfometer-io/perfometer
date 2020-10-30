package io.perfometer.data.csv

internal class CsvParser(private val csvText: String, private val delimiter: Char = ',') {

    private var position = 0

    fun parse(): List<List<String>> {
        position = 0
        val result = mutableListOf<List<String>>()
        while (notFinished()) {
            result.add(parseRecord())
        }
        return result
    }

    private fun parseRecord(): List<String> {
        val result = mutableListOf<String>()
        while (checkCurrentCharIsNot('\n')) {
            result.add(parseField())
        }
        while (checkCurrentCharIs('\n', '\r')) position++
        return result
    }

    private fun parseField(): String {
        return if (csvText[position] == '"') parseQuotedField() else parseUnquotedField()
    }

    private fun parseQuotedField(): String {
        position++
        val start = position
        while (checkCurrentCharIsNot('"') || checkNextCharIs('"')) {
            if (checkCurrentCharIs('"') && checkNextCharIs('"')) position++
            position++
        }
        val end = position
        position++
        while (checkCurrentCharIsNot(delimiter, '\n')) position++
        if (checkCurrentCharIs(delimiter)) position++
        return csvText.substring(start, end).replace("\"\"", "\"")
    }

    private fun parseUnquotedField(): String {
        val start = position
        while (checkCurrentCharIsNot(delimiter, '\n')) position++
        val end = position
        if (checkCurrentCharIs(delimiter)) position++
        return csvText.substring(start, end)
    }

    private fun checkCurrentCharIs(vararg chars: Char): Boolean =
        notFinished() && chars.contains(csvText[position])

    private fun checkCurrentCharIsNot(vararg chars: Char): Boolean =
        notFinished() && !chars.contains(csvText[position])

    private fun checkNextCharIs(vararg chars: Char): Boolean =
        position + 1 < csvText.length && chars.contains(csvText[position+1])

    private fun notFinished() = position < csvText.length
}
