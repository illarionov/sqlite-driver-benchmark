/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.gradle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.nio.file.LinkOption
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.walk

open class ConvertBenchmarkReportsTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
) : DefaultTask() {
    @get:InputDirectory
    @get:Option(option = "reports", description = "Directory with JSON reports")
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val reports: DirectoryProperty = objects.directoryProperty().convention(
        layout.buildDirectory.dir("outputs/connected_android_test_additional_output")
    )

    @get:OutputDirectory
    val outputDirectory: DirectoryProperty = objects.directoryProperty().convention(
        layout.buildDirectory.dir("outputs/vega_reports")
    )

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun execute() {
        reports.get().asFile.toPath()
            .walk()
            .filter {
                it.isRegularFile(LinkOption.NOFOLLOW_LINKS) &&
                        it.extension.equals("json", true)
            }
            .forEach { convertBenchmarkReportToVega(it) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun convertBenchmarkReportToVega(
        jsonFile: Path
    ) {
        val report = jsonFile.inputStream(LinkOption.NOFOLLOW_LINKS).buffered().use {
            json.decodeFromStream<BenchmarkReport>(it)
        }
        val vegaReport = report.toVegaReport()
        outputDirectory.file(jsonFile.name).get().asFile.outputStream().buffered().use {
            json.encodeToStream(vegaReport, it)
        }
    }
}

private fun BenchmarkReport.toVegaReport(): VegaLiteReport {
    val reports = benchmarks.map { benchmark: Benchmark ->
        val testName = benchmark.name
        val className = benchmark.className.substringAfterLast(".")
        val driverGroup = DRIVER_SUFFIXES.entries.firstNotNullOfOrNull { (prefix, driverName) ->
            if (className.endsWith(prefix)) {
                driverName to className.substringBefore(prefix)
            } else {
                null
            }
        } ?: (className to className)
        val runs = benchmark.metrics.timeNs
        VegaLiteRun(
            test = testName,
            group = driverGroup.second,
            driver = driverGroup.first,
            minimum = runs.minimum.toLong(),
            maximum = runs.maximum.toLong(),
            median = runs.median,
            coefficientOfVariation = runs.coefficientOfVariation
        )
    }
    return VegaLiteReport(
        reports.sortedWith(compareBy(VegaLiteRun::group, VegaLiteRun::test, VegaLiteRun::driver))
    )
}

private val DRIVER_SUFFIXES: Map<String, String> = mapOf(
    "ChasmNoFusionInterpreterDriver" to "ChasmNoFusion",
    "ChasmInterpreterDriver" to "Chasm",
    "ByteArrayAotDriver" to "ChicoryAotBA",
    "AotDriver" to "ChicoryAot",
    "ByteArrayInterpreterDriver" to "ChicoryBA",
    "ChicoryInterpreterDriver" to "Chicory",
    "AndroidDriver" to "Android",
    "BundledDriver" to "Bundled",
)

@Serializable
internal data class BenchmarkReport(
    val context: BenchmarkContext,
    val benchmarks: List<Benchmark>,
)

@Serializable
internal data class BenchmarkContext(
    val build: JsonObject,
    val cpuCoreCount: Int
)

@Serializable
internal data class Benchmark(
    val name: String,
    val params: Map<String, String>,
    val className: String,
    val totalRunTimeNs: Long,
    val metrics: BenchmarkMertrics,
    val sampledMetrics: Map<String, String>,
    val warmupIterations: Int,
    val repeatIterations: Int,
    val thermalThrottleSleepSeconds: Int,
    val profilerOutputs: List<BenchmarkProfilerOutputs>,
)

@Serializable
internal data class BenchmarkMertrics(
    val timeNs: BenchmarkRuns,
    val allocationCount: BenchmarkRuns,
)

@Serializable
internal data class BenchmarkRuns(
    val minimum: Double,
    val maximum: Double,
    val median: Double,
    val coefficientOfVariation: Double,
    val runs: List<Double>
)

@Serializable
internal data class BenchmarkProfilerOutputs(
    val type: String,
    val label: String,
    val filename: String,
)

@Serializable
internal data class VegaLiteReport(
    val values: List<VegaLiteRun>,
)

@Serializable
internal data class VegaLiteRun(
    val test: String,
    val group: String,
    val driver: String,
    val minimum: Long,
    val maximum: Long,
    val median: Double,
    val coefficientOfVariation: Double,
)
