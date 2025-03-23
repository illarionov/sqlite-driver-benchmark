/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MicrobenchmarkConfig
import at.released.sqlitedriverbenchmark.Benchmarks.BenchmarksConfig
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_1000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_100

@OptIn(ExperimentalBenchmarkConfigApi::class)
private val INTERPRETERS_BENCHMARK_CONFIG = BenchmarksConfig(
    createDatabaseMaxInsertEntries = 1000,
    selectWithPagingStep = 10,
    selectWithPagingHashCount = GAMES_HASH_100,
    companiesHashCount = COMPANIES_HASH_1000,
    microbenchmarkConfig = MicrobenchmarkConfig(warmupCount = 5, measurementCount = 10),
)

// AndroidDriver for reference
@InterpreterDrivers
class InterpreterBenchmarksAndroidDriver : Benchmarks(
    driverFactory = ::createAndroidSqliteDriver,
    driverName = "IsAndroidSQLiteDriver",
    driverHasExplainQuery = false,
    config = INTERPRETERS_BENCHMARK_CONFIG,
)

// Chicory AOT Driver for reference
@InterpreterDrivers
class InterpreterBenchmarksChicoryAotDriver : Benchmarks(
    driverFactory = ::createChicoryAotDriver,
    driverName = "IsChicoryAotDriver",
    driverHasExplainQuery = true,
    config = INTERPRETERS_BENCHMARK_CONFIG,
)

@InterpreterDrivers
class InterpreterBenchmarksChicoryInterpreterDriver : Benchmarks(
    driverFactory = ::createChicoryInterpreterDriver,
    driverName = "IsChicoryInterpreterDriver",
    driverHasExplainQuery = true,
    config = INTERPRETERS_BENCHMARK_CONFIG,
)

@InterpreterDrivers
class InterpreterBenchmarksChasmInterpreterDriver : Benchmarks(
    driverFactory = ::createChasmInterpreterDriver,
    driverName = "IsChasmInterpreterDriver",
    driverHasExplainQuery = true,
    config = INTERPRETERS_BENCHMARK_CONFIG,
)
