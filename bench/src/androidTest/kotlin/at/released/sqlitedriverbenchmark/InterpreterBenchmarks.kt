/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import at.released.sqlitedriverbenchmark.database.benchmarkCreateDatabase
import at.released.wasm.sqlite.binary.SqliteWasmEmscripten349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349Machine
import at.released.wasm.sqlite.binary.base.WasmSqliteConfiguration
import at.released.wasm.sqlite.driver.WasmSQLiteDriver
import at.released.wasm.sqlite.open.helper.chasm.ChasmSqliteEmbedder
import at.released.wasm.sqlite.open.helper.chicory.ChicorySqliteEmbedder
import io.github.charlietap.chasm.config.RuntimeConfig
import org.junit.Test

private const val MAX_INTERPRETER_INSERT_ENTITIES = 100

@InterpreterDrivers
class InterpreterBenchmarks : BaseBenchmarks() {
    // BundledDriver for reference
    @Test
    fun interpreters_create_database_BundledDriver() {
        val driver = BundledSQLiteDriver()
        benchmarkCreateDatabase(driver, "IsBundledDriver", MAX_INTERPRETER_INSERT_ENTITIES)
    }

    // FrameworkDriver for reference
    @Test
    fun interpreters_create_database_FrameworkDriver() {
        val driver = AndroidSQLiteDriver()
        benchmarkCreateDatabase(driver, "IsAndroidSQLiteDriver", MAX_INTERPRETER_INSERT_ENTITIES)
    }

    // ChicoryAot for reference
    @Test
    fun interpreters_create_database_ChicoryAot() {
        val driver = WasmSQLiteDriver(ChicorySqliteEmbedder) {
            openParams {
                openFlags = setOf()
            }
            embedder {
                sqlite3Binary = SqliteWasmEmscriptenAot349
                machineFactory = ::SqliteWasmEmscriptenAot349Machine
            }
        }
        benchmarkCreateDatabase(driver, "IsChicoryAot349", MAX_INTERPRETER_INSERT_ENTITIES)
    }

    @Test
    fun interpreters_create_database_Chicory() {
        val driver = WasmSQLiteDriver(ChicorySqliteEmbedder, context) {
            openParams {
                openFlags = setOf()
            }
            embedder {
                sqlite3Binary = object: WasmSqliteConfiguration by SqliteWasmEmscripten349 {
                    override val wasmMinMemorySize = 64 * 1024 * 1024L
                }
            }
        }
        benchmarkCreateDatabase(driver, "IsChicory349", MAX_INTERPRETER_INSERT_ENTITIES)
    }

    @Test
    fun interpreters_create_database_Chasm() {
        val driver = WasmSQLiteDriver(ChasmSqliteEmbedder, context) {
            openParams {
                openFlags = setOf()
            }
            embedder {
                sqlite3Binary = object: WasmSqliteConfiguration by SqliteWasmEmscripten349 {
                    override val wasmMinMemorySize = 64 * 1024 * 1024L
                }
                runtimeConfig = RuntimeConfig(bytecodeFusion = true)
            }
        }
        benchmarkCreateDatabase(driver, "IsChasm349", MAX_INTERPRETER_INSERT_ENTITIES)
    }
}
