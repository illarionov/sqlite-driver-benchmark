/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.content.Context
import android.util.Log
import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MicrobenchmarkConfig
import androidx.sqlite.SQLiteDriver
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.COMPANIES_HASH_1_000_000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.GAMES_HASH_1000
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.HashWithCount
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.SELECT_COMPANIES_STATEMENT
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.SELECT_GAMES_REQUEST
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.calculateCompaniesHash
import at.released.sqlitedriverbenchmark.database.RawgDatabaseGameDao.Companion.calculateGamesHash
import at.released.sqlitedriverbenchmark.database.execute
import at.released.sqlitedriverbenchmark.database.queryForString
import at.released.sqlitedriverbenchmark.database.queryTable
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.concurrent.atomic.AtomicLong

private const val TAG = "SqliteBenchmark"

@OptIn(ExperimentalBenchmarkConfigApi::class)
abstract class Benchmarks(
    val driverFactory: (Context) -> SQLiteDriver,
    val driverName: String,
    val driverHasExplainQuery: Boolean,
    val config: BenchmarksConfig,
) : BaseBenchmarksTest(config.microbenchmarkConfig) {
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
            context.createRawgDatabaseFromCsv(this, config.createDatabaseMaxInsertEntries)
            insertEntities = queryForString("SELECT COUNT(id) from game")
        }
        Log.i(TAG, "$driverName entities: $insertEntities")
    }

    @Test
    open fun select_with_paging() {
        var gamesHash: HashWithCount? = null
        val testDatabasePath = context.copyPreparedRawgDatabase(
            File(tempFolder.root, "db-${driverName}-selectwithpaging.sqlite")
        )

        if (driverHasExplainQuery) {
            val queryPlan = driver.execute(testDatabasePath) {
                queryTable("EXPLAIN QUERY PLAN $SELECT_GAMES_REQUEST")
            }
            Log.i(TAG, "$driverName Select games query plan: $queryPlan")
        }

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
        val testDatabasePath = context.copyPreparedRawgDatabase(
            File(tempFolder.root, "db-${driverName}-selectcompanies.sqlite")
        )

        if (driverHasExplainQuery) {
            val queryPlan = driver.execute(testDatabasePath) {
                queryTable("EXPLAIN QUERY PLAN $SELECT_COMPANIES_STATEMENT")
            }
            Log.i(TAG, "$driverName Select companies query plan: $queryPlan")
        }

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
        val microbenchmarkConfig: MicrobenchmarkConfig = MicrobenchmarkConfig(warmupCount = 5),
    )
}
