/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_1_000_000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_1000

private const val MAX_INSERT_ENTRIES: Int = 20000

@NativeDrivers
class NativeBenchmarksBundledDriver : Benchmarks(
    driverFactory = ::createBundledSqliteDriver,
    driverName = "BundledSQLiteDriver",
    createDatabaseMasInsertEntries = MAX_INSERT_ENTRIES,
    selectWithPagingStep = 40,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_1_000_000,
)

@NativeDrivers
class NativeBenchmarksAndroidDriver : Benchmarks(
    driverFactory = ::createAndroidSqliteDriver,
    driverName = "AndroidSQLiteDriver",
    createDatabaseMasInsertEntries = MAX_INSERT_ENTRIES,
    selectWithPagingStep = 40,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_1_000_000,
)
