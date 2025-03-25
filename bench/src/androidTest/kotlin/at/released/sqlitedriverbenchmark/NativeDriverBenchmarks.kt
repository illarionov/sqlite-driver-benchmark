/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MicrobenchmarkConfig
import at.released.sqlitedriverbenchmark.Benchmarks.BenchmarksConfig
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_500_000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_3000
import org.junit.Test

@OptIn(ExperimentalBenchmarkConfigApi::class)
private val NATIVE_BENCHMARK_CONFIG = BenchmarksConfig(
    createDatabaseMaxInsertEntries = 25000,
    selectWithPagingStep = 20,
    selectWithPagingHashCount = GAMES_HASH_3000,
    companiesHashCount = COMPANIES_HASH_500_000,
    microbenchmarkConfig = MicrobenchmarkConfig(),
)

@NativeDrivers
class NativeBenchmarksBundledDriver : Benchmarks(
    driverFactory = ::createBundledSqliteDriver,
    driverName = "BundledSQLiteDriver",
    driverHasExplainQuery = true,
    config = NATIVE_BENCHMARK_CONFIG,
)

@NativeDrivers
class NativeBenchmarksAndroidDriver : Benchmarks(
    driverFactory = ::createAndroidSqliteDriver,
    driverName = "AndroidSQLiteDriver",
    driverHasExplainQuery = false,
    config = NATIVE_BENCHMARK_CONFIG,
)
