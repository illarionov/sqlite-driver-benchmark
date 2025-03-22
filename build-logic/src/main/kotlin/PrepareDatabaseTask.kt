/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the wasm-sqlite-open-helper project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.sqlitedriverbenchmark.gradle

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import at.released.sqlitedriverbenchmark.database.TestSqliteConnection.Companion.setupRawgDbDefaults
import at.released.sqlitedriverbenchmark.database.importRawgDatabase
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class PrepareDatabaseTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputCsv: RegularFileProperty

    @get:Input
    abstract val sqlFileName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun execute() {
        val csv = inputCsv.get().asFile
        val databaseFile = outputDirectory.get().file(sqlFileName.get()).asFile
        databaseFile.delete()
        BundledSQLiteDriver().open(databaseFile.toString()).use { connection: SQLiteConnection ->
            connection.setupRawgDbDefaults()
            csv.inputStream().use { csvInputStream ->
                importRawgDatabase(connection, csvInputStream)
                connection.execSQL("VACUUM")
            }
        }
    }
}
