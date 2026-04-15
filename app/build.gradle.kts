import java.util.Properties
import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val infuraProjectId = localProperties.getProperty("INFURA_PROJECT_ID", "")
val ethNetwork = localProperties.getProperty("ETH_NETWORK", "mainnet")

android {
    namespace = "com.example.getlatestblockdata"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.getlatestblockdata"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "INFURA_PROJECT_ID", "\"$infuraProjectId\"")
        buildConfigField("String", "ETH_NETWORK", "\"$ethNetwork\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("buildAPK") {
    dependsOn("assembleDebug")

    doLast {
        val apkDir = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        val outputDir = rootProject.file("build-dev")
        val apkDest = outputDir.resolve("app-getLatestBlockData.apk")

        if (!apkDir.exists()) {
            throw GradleException("APK output directory not found: $apkDir")
        }

        val apkSource = apkDir
            .walkTopDown()
            .firstOrNull { it.isFile && it.extension == "apk" }
            ?: throw GradleException("No APK found in: $apkDir")

        outputDir.mkdirs()
        apkSource.copyTo(apkDest, overwrite = true)

        println("Copied APK from: $apkSource")
        println("Copied APK to: $apkDest")
    }
}