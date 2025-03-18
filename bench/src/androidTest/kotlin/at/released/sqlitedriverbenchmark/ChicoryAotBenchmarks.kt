/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import at.released.sqlitedriverbenchmark.database.benchmarkCreateDatabase
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349Machine
import at.released.wasm.sqlite.driver.WasmSQLiteDriver
import at.released.wasm.sqlite.open.helper.chicory.ChicorySqliteEmbedder
import org.junit.Test

private const val MAX_AOT_INSERT_ENTRIES: Int = 500

@ChicoryDrivers
class ChicoryAotBenchmarks : BaseBenchmarks() {
    // BundledDriver for reference
    @Test
    fun chicory_aot_create_database_BundledDriver() {
        val driver = BundledSQLiteDriver()
        benchmarkCreateDatabase(driver, "aotBundledDriver", MAX_AOT_INSERT_ENTRIES)
    }

    // FrameworkDriver for reference
    @Test
    fun chicory_aot_create_database_FrameworkDriver() {
        val driver = AndroidSQLiteDriver()
        benchmarkCreateDatabase(driver, "aotAndroidSQLiteDriver", MAX_AOT_INSERT_ENTRIES)
    }

    @Test
    fun chicory_aot_create_database_ChicoryAot() {
        val driver = WasmSQLiteDriver(ChicorySqliteEmbedder) {
            openParams {
                openFlags = setOf()
            }
            embedder {
                sqlite3Binary = SqliteWasmEmscriptenAot349
                machineFactory = ::SqliteWasmEmscriptenAot349Machine
            }
        }
        benchmarkCreateDatabase(driver, "aotChicoryAot349", MAX_AOT_INSERT_ENTRIES)
    }
}
