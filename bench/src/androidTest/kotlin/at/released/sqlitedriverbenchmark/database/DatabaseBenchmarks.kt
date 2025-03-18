/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.database

import android.util.Log
import androidx.benchmark.junit4.measureRepeated
import androidx.sqlite.SQLiteDriver
import at.released.sqlitedriverbenchmark.BaseBenchmarks
import java.util.concurrent.atomic.AtomicLong

internal fun BaseBenchmarks.benchmarkCreateDatabase(
    driver: SQLiteDriver,
    driverName: String,
    maxInsertEntries: Int? = null,
) {
    var version: String? = null
    var insertEntities: String? = null
    val id = AtomicLong(1)
    benchmarkRule.measureRepeated {
        measureSQLiteDriverBlock(
            driver,
            tempFolder.newFile("db-${driverName}-${id.getAndAdd(1)}.sqlite")
        ) {
            version = version()
            val database = RawgDatabase(this, context.assets)
            database.loadDatabase(maxInsertEntries)
            insertEntities = queryForString("SELECT COUNT(id) from game")
        }
    }
    Log.i("SqliteBenchmark", "$driverName SQLite version: $version entities: $insertEntities")
}
