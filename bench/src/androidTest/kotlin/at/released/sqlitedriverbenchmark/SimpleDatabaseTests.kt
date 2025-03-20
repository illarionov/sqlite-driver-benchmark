/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.util.Log
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_1000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.calculateCompaniesHash
import at.released.sqlitedriverbenchmark.database.execute
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import kotlin.time.measureTime
import kotlin.use

@NativeDrivers
class SimpleDatabaseTests : BaseBenchmarksTest() {
    @Test
    fun runCompanies() {
        val driver = createChasmInterpreterDriver(context)
        val testDatabasePath = TestDatabaseHolder.createTestDatabase(
            context = context,
            dstFile = File(tempFolder.root, "db-selectwithpaging.sqlite")
        )
        Log.i("SqliteBenchmark", "createTestDatabase done")

        driver.execute(testDatabasePath) {
            Log.i("SqliteBenchmark", "Version: ${version()} done")

            val companiesHash: Long
            val time = measureTime {
                companiesHash = RawgDatabaseGameDao(this).use { gameDao ->
                    gameDao.calculateCompaniesHash(COMPANIES_HASH_1000.count.toLong())
                }
            }
            Log.i("SqliteBenchmark", "AndroidSQLiteDriver Games hash: $companiesHash time: $time")
            assertEquals(COMPANIES_HASH_1000.hash, companiesHash)
        }
    }
}
