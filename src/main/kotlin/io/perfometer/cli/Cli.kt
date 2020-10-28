package io.perfometer.cli

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.system.exitProcess

internal object PerfScriptConfiguration : ScriptCompilationConfiguration({
    defaultImports("io.perfometer.dsl.*")
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
})

@KotlinScript(fileExtension = "perf.kts", compilationConfiguration = PerfScriptConfiguration::class)
internal abstract class PerfScript

internal fun evalScenarioFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
    return BasicJvmScriptingHost().eval(
        scriptFile.toScriptSource(),
        PerfScriptConfiguration,
        null
    )
}

fun main(vararg args: String) {
    if (args.size != 1) {
        println("usage: java -jar perfometer.jar <scenario file>")
        exitProcess(1)
    }

    val scenarioFile = File(args[0])
    if (!scenarioFile.exists()) {
        println("File $scenarioFile does not exist")
        exitProcess(1)
    }

    println("Executing scenario from file $scenarioFile")

    val res = evalScenarioFile(scenarioFile)

    res.reports.forEach {
        println(": ${it.message} ${if (it.exception == null) "" else " : ${it.exception}"}")
    }
}
