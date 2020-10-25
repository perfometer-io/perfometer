import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

object Versions {
    const val ktor = "1.4.0"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")
    implementation("io.ktor:ktor-client-cio:${Versions.ktor}")

    testImplementation(kotlin("test-junit"))
    testImplementation("io.kotest:kotest-runner-junit5:4.3.0")
    testImplementation("io.ktor:ktor-server-core:${Versions.ktor}")
    testImplementation("io.ktor:ktor-server-netty:${Versions.ktor}")
}

repositories {
    mavenCentral()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
