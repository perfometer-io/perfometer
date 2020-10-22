package io.perfometer.internal.helper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.perfometer.exception.InvalidScenarioConfigurationException
import java.net.URL
import kotlin.test.Test

class ParsersKtSpecification {

    @Test
    fun `should parse String to expected URL`() {
        listOf(
                "https://google.com" to URL("https://google.com"),
                "http://localhost:8080" to URL("http://localhost:8080"),
                "https://localhost:8443" to URL("https://localhost:8443"),
                "http://example.com" to URL("http://example.com"),
        ).forEach { (urlString, expectedUrl) -> urlString.toUrl() shouldBe expectedUrl }
    }

    @Test
    fun `should throw InvalidScenarioConfiguration given invalid URL String`() {
        listOf(
                "",
                "\t",
                "    ",
                "\n",
                "htt://localhost:8080",
                "localhost:8080",
        ).forEach { urlString -> shouldThrow<InvalidScenarioConfigurationException> { urlString.toUrl() } }
    }
}

