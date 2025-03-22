/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_1000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_1000

private const val MAX_INTERPRETER_INSERT_ENTITIES = 1000

// AndroidDriver for reference
@InterpreterDrivers
class InterpreterBenchmarksAndroidDriver : Benchmarks(
    driverFactory = ::createAndroidSqliteDriver,
    driverName = "IsAndroidSQLiteDriver",
    createDatabaseMaxInsertEntries = MAX_INTERPRETER_INSERT_ENTITIES,
    selectWithPagingStep = 40,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_1000,
)

// Chicory AOT Driver for reference
@InterpreterDrivers
class InterpreterBenchmarksChicoryAotDriver : Benchmarks(
    driverFactory = ::createChicoryAotDriver,
    driverName = "IsChicoryAotDriver",
    createDatabaseMaxInsertEntries = MAX_INTERPRETER_INSERT_ENTITIES,
    selectWithPagingStep = 50,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_1000,
)

@InterpreterDrivers
class InterpreterBenchmarksChicoryInterpreterDriver : Benchmarks(
    driverFactory = ::createChicoryInterpreterDriver,
    driverName = "IsChicoryInterpreterDriver",
    createDatabaseMaxInsertEntries = MAX_INTERPRETER_INSERT_ENTITIES,
    selectWithPagingStep = 50,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_1000,
)

@InterpreterDrivers
class InterpreterBenchmarksChasmInterpreterDriver : Benchmarks(
    driverFactory = ::createChasmInterpreterDriver,
    driverName = "IsChasmInterpreterDriver",
    createDatabaseMaxInsertEntries = MAX_INTERPRETER_INSERT_ENTITIES,
    selectWithPagingStep = 40,
    selectWithPagingHashCount = GAMES_HASH_1000,
    companiesHashCount = COMPANIES_HASH_1000,
)
