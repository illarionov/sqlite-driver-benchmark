/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.content.Context
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import at.released.wasm.sqlite.binary.SqliteWasmEmscripten349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349
import at.released.wasm.sqlite.binary.aot.SqliteWasmEmscriptenAot349Machine
import at.released.wasm.sqlite.binary.base.WasmSqliteConfiguration
import at.released.wasm.sqlite.driver.WasmSQLiteDriver
import at.released.wasm.sqlite.open.helper.chasm.ChasmSqliteEmbedder
import at.released.wasm.sqlite.open.helper.chicory.ChicorySqliteEmbedder
import io.github.charlietap.chasm.config.RuntimeConfig

fun createBundledSqliteDriver(context: Context) = BundledSQLiteDriver()

fun createAndroidSqliteDriver(context: Context) = AndroidSQLiteDriver()

internal fun createChicoryAotDriver(context: Context): SQLiteDriver = WasmSQLiteDriver(ChicorySqliteEmbedder) {
    openParams {
        openFlags = setOf()
    }
    embedder {
        sqlite3Binary = object : WasmSqliteConfiguration by SqliteWasmEmscriptenAot349 {
            override val wasmMinMemorySize: Long = 256 * 1024 * 1024
        }
        machineFactory = ::SqliteWasmEmscriptenAot349Machine
    }
}

internal fun createChicoryInterpreterDriver(context: Context): SQLiteDriver {
    return WasmSQLiteDriver(ChicorySqliteEmbedder, context) {
        openParams { openFlags = setOf() }
        embedder {
            sqlite3Binary = object : WasmSqliteConfiguration by SqliteWasmEmscripten349 {
                override val wasmMinMemorySize: Long = 256 * 1024 * 1024
            }
        }
    }
}

internal fun createChasmInterpreterDriver(context: Context): SQLiteDriver {
    return WasmSQLiteDriver(ChasmSqliteEmbedder, context) {
        openParams { openFlags = setOf() }
        embedder {
            sqlite3Binary = object : WasmSqliteConfiguration by SqliteWasmEmscripten349 {
                override val wasmMinMemorySize = 256 * 1024 * 1024L
            }
            runtimeConfig = RuntimeConfig(bytecodeFusion = true)
        }
    }
}
