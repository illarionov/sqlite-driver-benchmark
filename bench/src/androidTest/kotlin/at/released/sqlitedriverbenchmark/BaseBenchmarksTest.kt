/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalBenchmarkConfigApi::class)
abstract class BaseBenchmarksTest constructor(
    microbenchmarkConfig: MicrobenchmarkConfig = MicrobenchmarkConfig(warmupCount = 5)
) {
    @OptIn(ExperimentalBenchmarkConfigApi::class)
    @get:Rule
    val benchmarkRule = BenchmarkRule(config = microbenchmarkConfig)

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder(context.cacheDir)
}
