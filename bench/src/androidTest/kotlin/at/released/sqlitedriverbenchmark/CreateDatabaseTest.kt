/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import at.released.sqlitedriverbenchmark.database.measureSQLiteDriverBlock
import at.released.wasm.sqlite.binary.SqliteWasmEmscripten349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349Machine
import at.released.wasm.sqlite.binary.base.WasmSqliteConfiguration
import at.released.wasm.sqlite.driver.WasmSQLiteDriver
import at.released.wasm.sqlite.open.helper.chasm.ChasmSqliteEmbedder
import at.released.wasm.sqlite.open.helper.chicory.ChicorySqliteEmbedder
import io.github.charlietap.chasm.config.RuntimeConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateDatabaseTest {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun benchmarkCreateDatabase_BundledDriver() {
        val driver = BundledSQLiteDriver()
        var version: String? = null
        benchmarkRule.measureRepeated {
            version = measureSQLiteDriverBlock(driver, null) {
                repeat(1000) {
                    version()
                }
                version()
            }
        }
        Log.i("SqliteBenchmark", "BundledSQLiteDriver version: $version")
    }

    @Test
    fun benchmarkCreateDatabase_FrameworkDriver() {
        val driver = AndroidSQLiteDriver()
        var version: String? = null
        benchmarkRule.measureRepeated {
            version = measureSQLiteDriverBlock(driver, null) {
                repeat(1000) {
                    version()
                }
                version()
            }
        }
        Log.i("SqliteBenchmark", "AndroidSQLiteDriver version: $version")
    }

    @Test
    fun benchmarkCreateDatabase_ChicoryAot() {
        val driver = WasmSQLiteDriver(ChicorySqliteEmbedder) {
            openParams {
                openFlags = setOf()
            }
            embedder {
                sqlite3Binary = SqliteWasmEmscriptenAot349
                machineFactory = ::SqliteWasmEmscriptenAot349Machine
            }
        }
        var version: String? = null
        benchmarkRule.measureRepeated {
            version = measureSQLiteDriverBlock(driver, null) {
                repeat(1000) {
                    version()
                }
                version()
            }
        }
        Log.i("SqliteBenchmark", "ChicoryAot349 version: $version")
    }

    @Test
    fun benchmarkCreateDatabase_Chicory() {
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
        var version: String? = null
        benchmarkRule.measureRepeated {
            version = measureSQLiteDriverBlock(driver, null) {
                repeat(1000) {
                    version()
                }
                version()
            }
        }
        Log.i("SqliteBenchmark", "Chicory349 version: $version")
    }

    @Test
    fun benchmarkCreateDatabase_Chasm() {
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
        var version: String? = null
        benchmarkRule.measureRepeated {
            version = measureSQLiteDriverBlock(driver, null) {
                repeat(1000) {
                    version()
                }
                version()
            }
        }
        Log.i("SqliteBenchmark", "Chasm349 version: $version")
    }
}
