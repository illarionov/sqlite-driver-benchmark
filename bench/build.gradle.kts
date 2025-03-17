plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.androidx.benchmark)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "at.released.sqlitedriverbenchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.profiling.mode"] = "None"
    }

    testBuildType = "benchmark"

    testOptions {
        targetSdk = 35
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            testProguardFiles += listOf(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-test-rules.pro"),
            )
        }
        create("benchmark") {
            initWith(buildTypes["release"])
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variantBuilder ->
        variantBuilder.hostTests.forEach { (_, builder) -> builder.enable = false }
    }
}

dependencies {
    implementation(libs.androidx.benchmark)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.chicory.runtime)
    implementation(libs.wsoh.sqlite.binary)
    implementation(libs.wsoh.sqlite.binary.plain)
    implementation(libs.wsoh.sqlite.driver)
    implementation(libs.wsoh.sqlite.embedder.chicory)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
