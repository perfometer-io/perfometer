package io.perfometer.data

import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class DataProviderSpecification() {

    @Test
    fun `circular strategy should increment index by 1 and go back to zero when reaching size`() {
        val size = 3
        with(CircularDataProviderStrategy()) {
            nextIndex(size) shouldBe 0
            nextIndex(size) shouldBe 1
            nextIndex(size) shouldBe 2
            nextIndex(size) shouldBe 0
        }
    }
}
