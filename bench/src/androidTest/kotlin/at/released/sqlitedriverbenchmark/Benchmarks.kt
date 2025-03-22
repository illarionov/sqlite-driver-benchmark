/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.content.Context
import android.util.Log
import androidx.sqlite.SQLiteDriver
import at.released.sqlitedriverbenchmark.database.RawgDatabase
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_1_000_000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_1000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.HashWithCount
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.calculateCompaniesHash
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.calculateGamesHash
import at.released.sqlitedriverbenchmark.database.measureRepeatedSQLiteDriverBlock
import at.released.sqlitedriverbenchmark.database.queryForString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.concurrent.atomic.AtomicLong

abstract class Benchmarks(
    val driverFactory: (Context) -> SQLiteDriver,
    val driverName: String,
    val config: BenchmarksConfig,
) : BaseBenchmarksTest() {
    val driver: SQLiteDriver get() = driverFactory(context)

    @Test
    open fun create_database() {
        var insertEntities: String? = null
        val id = AtomicLong(1)
        benchmarkRule.measureRepeatedSQLiteDriverBlock(
            driver = driver,
            path = {
                File(
                    this@Benchmarks.tempFolder.root,
                    "db-${driverName}-${id.getAndAdd(1)}.sqlite"
                )
            },
        ) {
            val database = RawgDatabase(this, this@Benchmarks.context.assets)
            database.createDatabaseFromAssets(config.createDatabaseMaxInsertEntries)
            insertEntities = queryForString("SELECT COUNT(id) from game")
        }
        Log.i("SqliteBenchmark", "$driverName entities: $insertEntities")
    }

    @Test
    open fun select_with_paging() {
        var gamesHash: HashWithCount? = null
        val testDatabasePath = TestDatabaseHolder.createTestDatabase(
            context = context,
            dstFile = File(tempFolder.root, "db-${driverName}-selectwithpaging.sqlite")
        )
        benchmarkRule.measureRepeatedSQLiteDriverBlock(
            driver = driver,
            path = { testDatabasePath }
        ) {
            gamesHash = RawgDatabaseGameDao(this).use { gameDao ->
                gameDao.calculateGamesHash(
                    maxEntries = config.selectWithPagingHashCount.count,
                    step = config.selectWithPagingStep
                )
            }
        }
        assertEquals(config.selectWithPagingHashCount, gamesHash)
    }

    @Test
    open fun huge_select() {
        val testDatabasePath = TestDatabaseHolder.createTestDatabase(
            context = context,
            dstFile = File(tempFolder.root, "db-${driverName}-selectcompanies.sqlite")
        )
        var companiesHash: Long = 0
        benchmarkRule.measureRepeatedSQLiteDriverBlock(driver, { testDatabasePath }) {
            companiesHash = RawgDatabaseGameDao(this).use { gameDao ->
                gameDao.calculateCompaniesHash(config.companiesHashCount.count.toLong())
            }
        }
        assertEquals(config.companiesHashCount.hash, companiesHash)
    }

    public data class BenchmarksConfig(
        val createDatabaseMaxInsertEntries: Int = 20000,
        val selectWithPagingStep: Int = 40,
        val selectWithPagingHashCount: HashWithCount = GAMES_HASH_1000,
        val companiesHashCount: HashWithCount = COMPANIES_HASH_1_000_000,
    )
}
