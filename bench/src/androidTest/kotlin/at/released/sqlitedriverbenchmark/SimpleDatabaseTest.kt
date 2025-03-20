/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import androidx.annotation.Keep
import androidx.sqlite.driver.AndroidSQLiteDriver
import at.released.sqlitedriverbenchmark.database.RawgDatabase
import at.released.sqlitedriverbenchmark.database.run
import org.junit.Test

@NativeDrivers
@Keep
class SimpleDatabaseTest : BaseBenchmarks() {
    @Test
    fun createDatabase() {
        val databaseFile = tempFolder.newFile("rawg.sqlite")
        val driver = AndroidSQLiteDriver()
        driver.run(databaseFile) {
            val database = RawgDatabase(this, context.assets)
            database.loadDatabase()
        }
    }
}
