/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import at.released.sqlitedriverbenchmark.database.benchmarkCreateDatabase
import org.junit.Test

private val MAX_INSERT_ENTRIES: Int = 5000

@NativeDrivers
class NativeDriverBenchmarks : BaseBenchmarks() {
    @Test
    fun native_create_database_BundledDriver() {
        val driver = BundledSQLiteDriver()
        benchmarkCreateDatabase(driver, "BundledDriver", MAX_INSERT_ENTRIES)
    }

    @Test
    fun native_create_database_FrameworkDriver() {
        val driver = AndroidSQLiteDriver()
        benchmarkCreateDatabase(driver, "AndroidSQLiteDriver", MAX_INSERT_ENTRIES)
    }
}
