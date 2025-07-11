import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.loadProperties

val propertyFile = loadProperties("local.properties")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.kotlin.gradle)
        classpath(libs.buildkonfig.gradle)
    }
}

buildkonfig {
    packageName = "com.softwire.trainboard.utilities"
    objectName = "Config"

    defaultConfigs {
        buildConfigField(
            STRING,
            "API_KEY",
            propertyFile.getProperty("trainboard.api_key")
                ?: throw IllegalArgumentException("API_KEY not found in local.properties"),
            const = true,
        )
    }
}

detekt {
    toolVersion =
        libs.plugins.detekt
            .get()
            .version
            .toString()
    config.setFrom(file("gradle/.detekt.yml"))
    buildUponDefaultConfig = true

    dependencies {
        // Compose Ruleset (BUILD ONLY) | https://github.com/mrmans0n/compose-rules | Apache-2.0
        detektPlugins(libs.detekt.compose)
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_23)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // KotlinX | ... | usually Apache-2.0
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.datetime)

            // Kamel | https://github.com/Kamel-Media/Kamel | Apache-2.0
            // Images not loading? Don't forget to import other optional dependencies...
            implementation(libs.kamel.default)

            // Async Web Client | https://github.com/ktorio/ktor | Apache-2.0
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.serialization.json)

            // Skeleton / Shimmer Effect | https://github.com/valentinilk/compose-shimmer | Apache-2.0
            implementation(libs.shimmer)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            // [Common] Async Client | https://github.com/ktorio/ktor | Apache-2.0
            implementation(libs.ktor.client.cio)
        }

        iosMain.dependencies {
            // [Common] Async Client | https://github.com/ktorio/ktor | Apache-2.0
            implementation(libs.ktor.client.darwin)
        }
    }

    applyDefaultHierarchyTemplate()
}

android {
    namespace = "com.softwire.trainboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.softwire.trainboard"

        // Newer `ktor` versions require a minimum SDK of 30 - somewhere internally it uses spaces
        // in a [SimpleName] ('use streaming syntax'), which isn't allowed prior to DEX version 040.
        // https://stackoverflow.com/questions/75578780/com-android-tools-r8-internal-jc-space-characters-in-simplename-exception-are-n
        // https://kotlinlang.org/docs/coding-conventions.html#names-for-test-methods
        minSdk = 30

        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }

    buildFeatures {
        buildConfig = true
    }
}
