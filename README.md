# SQLite Driver Benchmarks

This project benchmarks the performance of various implementations of [SQLiteDriver][androidx.sqlite.SQLiteDriver] 
on Android devices using the [Android Microbenchmark] framework.

The test results are available on the [Benchmarks](https://wsoh.released.at/blog/benchmark) blog post.

The project includes three groups of tests.
The first group measures the performance of SQLiteDriver implementations based on native
SQLite compilations. The drivers included in this comparison are:

* *androidx.sqlite.driver.AndroidSQLiteDriver*
* *androidx.sqlite.driver.BundledSQLiteDriver*

The second and third groups focus on benchmarking SQLiteDriver implementations from the 
[Wasm SQLite Open Helper] project.

In the second group, SQLite is compiled to WebAssembly and then ahead-of-time compiled into 
JVM bytecode (.class). `WasmSQLiteDriver (ChicorySqliteEmbedder)`
runs within the Chicory WebAssembly Runtime.

A third group benchmarks SQLite running in various WebAssembly interpreters for the JVM:

* Chicory interpreter
* Chasm interpreter

The [rawg-games-dataset] is used as the database source. Please ensure that Git LFS is installed.

## Execution

```shell
./gradlew bench:conAT 
```

This command runs all benchmarks on the connected android device. 
A real device is required for execution.
The results can be found in the 
`bench/build/outputs/connected_android_test_additional_output/releaseAndroidTest/connected` directory.

The entire set of tests can take several hours to complete.

You can use the following commands to run limited sets of tests.

```shell
./gradlew bench:conAT -Pandroid.testInstrumentationRunnerArguments.annotation=at.released.sqlitedriverbenchmark.NativeDrivers
```

This command runs Native Driver benchmarks only.

```shell
./gradlew bench:conAT -Pandroid.testInstrumentationRunnerArguments.annotation=at.released.sqlitedriverbenchmark.ChicoryDrivers
```
This command runs Chicory AOT benchmarks only.

```shell
./gradlew bench:conAT -Pandroid.testInstrumentationRunnerArguments.annotation=at.released.sqlitedriverbenchmark.InterpreterDrivers

```
This command runs Interpreters benchmarks only.

[Android Microbenchmark]: https://developer.android.com/topic/performance/benchmarking/microbenchmark-overview
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory WebAssembly Runtime]: https://chicory.dev/
[ChicorySqliteEmbedder]: https://wsoh.released.at/embedders/Chicory
[Wasm SQLite Open Helper]: https://wsoh.released.at/
[androidx.sqlite.SQLiteDriver]: https://developer.android.com/reference/kotlin/androidx/sqlite/SQLiteDriver
[rawg-games-dataset]: https://huggingface.co/datasets/atalaydenknalbant/rawg-games-dataset
