package io.perfometer.data.csv

import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class CsvParserSpecification {

    @Test
    fun `should parse unquoted csv`() {
        // given
        val csvText = """
            1,Marcin,Nowak
            2,Jan,Kowalski
            3,Bartek,Bąkiewicz
        """.trimIndent()

        // when
        val result = CsvParser(csvText).parse()

        // then
        result shouldBe listOf(
            listOf("1", "Marcin", "Nowak"),
            listOf("2", "Jan", "Kowalski"),
            listOf("3", "Bartek", "Bąkiewicz"),
        )
    }

    @Test
    fun `should parse quoted csv`() {
        // given
        val csvText = """
            "1","Marcin,Maria","Nowak"
            "2","Jan ""Zły""${'"'},"Kowalski"
            "3"," Bartek ","Bąkiewicz"
        """.trimIndent()

        // when
        val result = CsvParser(csvText).parse()

        // then
        result shouldBe listOf(
            listOf("1", "Marcin,Maria", "Nowak"),
            listOf("2", "Jan \"Zły\"", "Kowalski"),
            listOf("3", " Bartek ", "Bąkiewicz"),
        )
    }

    @Test
    fun `should parse csv with semicolon as delimiter`() {
        // given
        val csvText = """
            1;Marcin;Nowak
            2;Jan;Kowalski
            3;Bartek;Bąkiewicz
        """.trimIndent()

        // when
        val result = CsvParser(csvText, ';').parse()

        // then
        result shouldBe listOf(
            listOf("1", "Marcin", "Nowak"),
            listOf("2", "Jan", "Kowalski"),
            listOf("3", "Bartek", "Bąkiewicz"),
        )
    }
}
