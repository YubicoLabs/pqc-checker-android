import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.yubico.labs.pqc.checker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.yubico.labs.pqc.checker"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                signingConfig = null

                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

tasks.register<Exec>("signPqcRelease") {
    description = "Sign release APKs with PQC"
    dependsOn("assembleRelease")

    val unsignedApk = layout.buildDirectory.file("outputs/apk/release/app-release-unsigned.apk").get().asFile
    val keystore = file("keystore.jks")

    commandLine(
        "apksigner", "sign",
        "--ks", keystore.absolutePath, "--ks-key-alias", "release",
        "--hybrid-signer-role", "classical",
        "--next-signer",
        "--ks", keystore.absolutePath, "--ks-key-alias", "release_pqc",
        "--hybrid-signer-role", "pqc",
        unsignedApk.absolutePath
    )
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.accompanist.drawablepainter)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
