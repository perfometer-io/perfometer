import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val kotlin = "1.4.10"
    const val kotlinCoroutines = "1.4.0-M1"
    const val ktor = "1.4.0"
    const val kotest = "4.3.0"
}

plugins {
    kotlin("jvm") version Versions.kotlin
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktor}")

    testImplementation(kotlin("test-junit"))
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
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
