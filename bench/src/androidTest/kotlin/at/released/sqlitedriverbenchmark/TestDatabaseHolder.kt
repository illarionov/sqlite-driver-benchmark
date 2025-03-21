/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark

import android.content.Context
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import at.released.sqlitedriverbenchmark.database.RawgDatabase
import at.released.sqlitedriverbenchmark.database.execute
import java.io.File

object TestDatabaseHolder {
    private const val BLANK_DATABASE_NAME = "rawg-blank.sqlite"
    private val lock = Any()
    private var testDatabasePath: File? = null

    fun createTestDatabase(
        context: Context,
        dstFile: File
    ): File = synchronized(lock) {
        val blankDatabase = getDatabaseBlank(context)
        blankDatabase.copyTo(dstFile, overwrite = false)
        return dstFile
    }

    private fun getDatabaseBlank(context: Context): File = synchronized(lock) {
        return testDatabasePath.let { cachedPath ->
            cachedPath ?: prepareBlankDatabase(context).also {
                testDatabasePath = it
            }
        }
    }

    private fun prepareBlankDatabase(context: Context): File {
        val databaseFile = File(context.cacheDir, BLANK_DATABASE_NAME)
        val driver = BundledSQLiteDriver()
        driver.execute(databaseFile) {
            val database = RawgDatabase(this, context.assets)
            database.createDatabaseFromAssets()
        }
        return databaseFile
    }
}
