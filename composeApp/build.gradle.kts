import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.animation)
            implementation(compose.ui)

            implementation(libs.voyager.navigator)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.googleid)
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.view)
            implementation(libs.camerax.mlkit)
            implementation(libs.mlkit.barcode)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                runtimeOnly(libs.slf4j.nop)
            }
        }
    }
}

android {
    namespace = "com.racion.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.racion.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.racion.app.MainKt"
    }
}
