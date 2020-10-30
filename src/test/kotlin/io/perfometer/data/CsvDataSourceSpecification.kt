package io.perfometer.data

import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class CsvDataSourceSpecification {

    data class Dict(
        val id: Int,
        val code: String,
        val name: String,
        val value: Long,
    )

    @Test
    fun `should read data from CSV file to data class objects`() {
        // when
        val data = CsvDataSource(
            Dict::class,
            this::class.java.getResource("dict.csv").path,
            hasHeader = true,
        )

        // then
        with(data) {
            size shouldBe 3
            with(get(0)) {
                id shouldBe 1
                code shouldBe "foo"
                name shouldBe "Foo"
                value shouldBe 11
            }
            with(get(1)) {
                id shouldBe 2
                code shouldBe "bar"
                name shouldBe "Bar"
                value shouldBe 22
            }
            with(get(2)) {
                id shouldBe 3
                code shouldBe "ala"
                name shouldBe "Ala ma kota"
                value shouldBe 33
            }
        }
    }
}
