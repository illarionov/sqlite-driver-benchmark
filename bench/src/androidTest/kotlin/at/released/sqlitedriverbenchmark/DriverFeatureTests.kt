/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.util.Log
import androidx.annotation.Keep
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import androidx.sqlite.execSQL
import at.released.sqlitedriverbenchmark.database.execute
import at.released.sqlitedriverbenchmark.database.queryForString
import at.released.sqlitedriverbenchmark.database.queryTable
import org.junit.Test

private const val TAG = "SqliteBenchmark"

@Keep
class DriverFeatureTests : BaseBenchmarksTest() {
    private val drivers = listOf(
        createAndroidSqliteDriver(context) to "AndroidSqliteDriver",
        createBundledSqliteDriver(context) to "BundledSqliteDriver"
    )

    @Test
    fun case_insensitive_like_do_not_work_with_BundledSqliteDriver() {
        drivers.forEach { (driver, driverName) ->
            val user = driver.execute {
                listOf(
                    """CREATE TABLE Customers(id INTEGER PRIMARY KEY, name TEXT, city TEST)""",
                    """INSERT INTO Customers(name,city) VALUES ("Пользователь", "Город")""",
                ).forEach(::execSQL)
                queryTable("SELECT name FROM Customers WHERE name LIKE 'пол%'")
            }
            Log.i(TAG, "$driverName: $user")
        }
    }

    @Test
    fun upper_lower_do_not_work_with_BundledSqliteDriver() {
        drivers.forEach { (driver, driverName) ->
            driver.execute {
                val upper = queryForString("""SELECT upper('пользователь')""")
                val lower = queryForString("""SELECT lower('ИЗДӨӨ')""")
                val lowerTr = queryTableSilent("""SELECT lower('ISPANAK', 'tr_tr')""")
                Log.i(TAG, "$driverName: $upper $lower $lowerTr")
            }
        }
    }

    @Test
    fun collate_locale_not_available_with_BundledSqliteDriver() {
        drivers.forEach { (driver, driverName) ->
            driver.execute {
                listOf(
                    """SELECT icu_load_collation('ru_RU', 'russian')""",
                    """CREATE TABLE Customers(name TEXT COLLATE russian)""",
                    """INSERT INTO Customers(name) VALUES ('Б'), ('а')""",
                ).forEach {
                    try {
                        execSQL(it)
                    } catch (e: SQLiteException) {
                        Log.e(TAG, "execSQL() failed", e)
                    }
                }
                val user = queryTableSilent("SELECT name FROM Customers ORDER BY name")
                Log.i(TAG, "$driverName: $user")
            }
        }
    }

    @Test
    fun regexp_not_available_with_BundledSqliteDriver() {
        drivers.forEach { (driver, driverName) ->
            driver.execute {
                listOf(
                    """CREATE TABLE Customers(name TEXT)""",
                    """INSERT INTO Customers(name) VALUES ('Пользователь 😎')""",
                ).forEach(::execSQL)
                val user = queryTableSilent(
                    """SELECT name FROM Customers WHERE name REGEXP '.+\p{Emoji}+'""",
                )
                Log.i(TAG, "$driverName: $user")
            }
        }
    }

    @Test
    fun fts5_available_with_BundledSqliteDriver() {
        createBundledSqliteDriver(context).execute {
            listOf(
                """CREATE TABLE Customers(id INTEGER PRIMARY KEY, name TEXT, city TEXT)""",
                """CREATE VIRTUAL TABLE Customers_idx USING fts5(name, content='Customers', content_rowid='id')""",
                """INSERT INTO Customers(name,city) VALUES ('Пользователь 😎', 'Город')""",
                """INSERT INTO Customers(name,city) VALUES ('ISPANAK', 'Stambul')""",
                """INSERT INTO Customers_idx(Customers_idx) values('rebuild')""",
            ).forEach(::execSQL)
            val user = queryTable(
                """
                    SELECT Customers.*
                    FROM Customers_idx
                       INNER JOIN Customers ON Customers_idx.rowid=Customers.id
                    WHERE Customers_idx.name MATCH '"поль" *'
                    """.trimIndent()
            )
            Log.i(TAG, "$user")
        }
    }

    @Test
    fun undocumented_functions_available_with_AndroidSQLiteDriver() {
        createAndroidSqliteDriver(context).execute {
            execSQL("""CREATE TABLE Customers(id INTEGER PRIMARY KEY, name TEXT, phone TEXT COLLATE PHONEBOOK)""")
            val delete = queryTable("""SELECT _DELETE_FILE('testfile')""")
            val equal = queryTable("""SELECT PHONE_NUMBERS_EQUAL('1234','1234')""")
            val strippedReversed = queryTable("""SELECT _PHONE_NUMBER_STRIPPED_REVERSED('1234')""")
            Log.i(TAG, "$delete $equal $strippedReversed")
        }
    }

    @Test
    fun bytecode_vtab_dbstat_vtab_not_available_with_BundledSqliteDriver() {
        drivers.forEach { (driver, driverName) ->
            driver.execute {
                execSQL("""CREATE TABLE Customers(id INTEGER PRIMARY KEY, name TEXT)""")
                val bytecode =
                    queryTableSilent("""SELECT * FROM bytecode('SELECT * FROM Customers')""")
                val tablesUsed =
                    queryTableSilent("""SELECT * FROM tables_used('SELECT * FROM Customers')""")
                val dbstat = queryTableSilent("""SELECT * FROM dbstat""")
                Log.i(
                    TAG,
                    "$driverName: bytecode: $bytecode tablesUsed: $tablesUsed dbstat: $dbstat"
                )
            }
        }
    }

    @Test
    fun pragma_compile_options_not_available_with_AndroidSQLiteDriver() {
        drivers.forEach { (driver, driverName) ->
            driver.execute {
                val options = queryTableSilent("""PRAGMA compile_options""")
                Log.i(TAG, "$driverName: options: $options")
            }
        }
    }

    @Test
    fun json_available_with_BundledSqliteDriver() {
        createBundledSqliteDriver(context).execute {
            listOf(
                """CREATE TABLE Customers(id INTEGER PRIMARY KEY, data BLOB)""",
                """INSERT INTO Customers(data) VALUES (jsonb('{"city": "City", "name": "User"}'))""",
            ).forEach(::execSQL)
            val customer = queryTable(
                """SELECT id,json(data) FROM Customers WHERE data ->> '$.city' = 'City'""".trimIndent()
            )
            Log.i(TAG, "$customer")
        }
    }

    @Test
    fun rtree_available_with_BundledSqliteDriver() {
        createBundledSqliteDriver(context).execute {
            listOf(
                """CREATE TABLE Products(id INTEGER PRIMARY KEY, name TEXT NOT NULL)""",
                """CREATE VIRTUAL TABLE PriceRanges USING rtree(id, minPrice, maxPrice)""",
                """INSERT INTO Products (id, name) VALUES(1, 'Thermosiphon')""",
                """INSERT INTO PriceRanges VALUES(1, 115, 380)"""
            ).forEach(::execSQL)
            val products = queryTable(
                """SELECT Products.* FROM Products,PriceRanges ON Products.id=PriceRanges.id WHERE maxPrice>=300 AND minPrice <= 300""".trimIndent()
            )
            Log.i(TAG, "$products")
        }
    }

    @Test
    fun upsert_with_returning_available_with_BundledSqliteDriver() {
        createBundledSqliteDriver(context).execute {
            execSQL("""CREATE TABLE Customers(id INTEGER PRIMARY KEY, uuid TEXT UNIQUE, name TEXT)""")
            val id1 = queryTableSilent(
                "INSERT INTO Customers(uuid,name) VALUES ('123','Customer') " +
                        "ON CONFLICT(uuid) DO UPDATE SET name=excluded.name RETURNING id"
            )
            val id2 = queryTableSilent(
                "INSERT INTO Customers(uuid,name) VALUES ('123','Customer') " +
                        "ON CONFLICT(uuid) DO UPDATE SET name=excluded.name RETURNING id"
            )

            Log.i(TAG, "$id1 $id2")
        }
    }

    @Test
    fun new_time_functions_available_with_BundledSqliteDriver() {
        createBundledSqliteDriver(context).execute {
            val timediff = queryTableSilent("""SELECT timediff('2025-03-21','2025-01-01')""")
            val ts = queryTableSilent(
                """SELECT strftime('%F %k:%l', 1743290838, 'unixepoch', 'floor')"""
            )
            Log.i(TAG, "timediff: $timediff, ts: $ts")
        }
    }

    @Test
    fun pragma_table_list_available_with_BundledSqliteDriver() {
        createBundledSqliteDriver(context).execute {
            val tables = queryTableSilent("""PRAGMA table_list""")
            Log.i(TAG, tables)
        }
    }

    private companion object {
        fun SQLiteConnection.queryTableSilent(
            sql: String,
            vararg bindArgs: Any?,
        ): String = try {
            queryTable(sql, bindArgs = bindArgs).toString()
        } catch (ex: SQLiteException) {
            "queryTableSilent(`$sql`) failed: ${ex.toString()}"
        }
    }
}
