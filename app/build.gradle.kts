import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.plusAssign

plugins {
    alias(libs.plugins.android.application)
    // alias(libs.plugins.androidx.benchmark)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "at.released.sqlitedriverbenchmark"
    compileSdk = 35

    defaultConfig {
        applicationId = "at.released.sqlitedriverbenchmark"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.profiling.mode"] = "None"
    }

    testBuildType = "benchmark"

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
            isDebuggable = false
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
    buildFeatures {
        compose = true
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variantBuilder ->
        variantBuilder.hostTests.forEach { (_, builder) -> builder.enable = false }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.benchmark)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.chicory.runtime)
    implementation(libs.wsoh.sqlite.binary)
    implementation(libs.wsoh.sqlite.binary.plain)
    implementation(libs.wsoh.sqlite.driver)
    implementation(libs.wsoh.sqlite.embedder.chicory)
    implementation(platform(libs.androidx.compose.bom))
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
