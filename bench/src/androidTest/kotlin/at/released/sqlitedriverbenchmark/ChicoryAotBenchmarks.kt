/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import at.released.sqlitedriverbenchmark.Benchmarks.BenchmarksConfig
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_5000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_1000

private val CHICORY_AOT_BENCHMARK_CONFIG = BenchmarksConfig(
    createDatabaseMaxInsertEntries = 5000,
    selectWithPagingStep = 50,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_5000
)

// AndroidDriver for reference
@ChicoryDrivers
class ChicoryAotBenchmarksAndroidDriver : Benchmarks(
    driverFactory = ::createAndroidSqliteDriver,
    driverName = "ChicoryAotAndroidSQLiteDriver",
    config = CHICORY_AOT_BENCHMARK_CONFIG,
)

@ChicoryDrivers
class ChicoryAotBenchmarksAotDriver : Benchmarks(
    driverFactory = ::createChicoryAotDriver,
    driverName = "ChicoryAot349SQLiteDriver",
    config = CHICORY_AOT_BENCHMARK_CONFIG,
)

