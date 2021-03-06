import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

object Versions {
    const val kotlinCoroutines = "1.4.0-M1"
    const val ktor = "1.4.0"
    const val kotest = "4.3.0"
    const val kotlinHtml = "0.7.2"
}

repositories {
    mavenCentral()
    jcenter()
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))

    implementation("io.ktor:ktor-client-cio:${Versions.ktor}")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:${Versions.kotlinHtml}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}")

    testImplementation(kotlin("test-junit"))
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
    testImplementation("io.ktor:ktor-server-core:${Versions.ktor}")
    testImplementation("io.ktor:ktor-server-netty:${Versions.ktor}")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "io.perfometer.cli.CliKt"))
    }
}
