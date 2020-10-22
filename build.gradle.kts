import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")

    testImplementation(kotlin("test-junit"))
    testImplementation("io.kotest:kotest-runner-junit5:4.3.0")
    testImplementation("io.ktor:ktor-server-core:1.4.0")
    testImplementation("io.ktor:ktor-server-netty:1.4.0")
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
