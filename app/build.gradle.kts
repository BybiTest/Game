plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kalamepich"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val tapsellAppKey = System.getenv("TAPSELL_APP_KEY")
            ?: (project.findProperty("TAPSELL_APP_KEY") as? String)
            ?: "mcqrarnosbkggjjikbfpboggaitidsoteapnsrrgnfcjgnopapqbljbhmnkcmdmhfodkes"

        val tapsellRewardedZoneId = System.getenv("TAPSELL_REWARDED_ZONE_ID")
            ?: (project.findProperty("TAPSELL_REWARDED_ZONE_ID") as? String)
            ?: "6aa55a69cd33cd4ed6e43183"

        val tapsellBannerZoneId = System.getenv("TAPSELL_BANNER_ZONE_ID")
            ?: (project.findProperty("TAPSELL_BANNER_ZONE_ID") as? String)
            ?: "6aa55a83cd33cd4ed6e43184"

        buildConfigField("String", "TAPSELL_APP_KEY", "\"$tapsellAppKey\"")
        buildConfigField("String", "TAPSELL_REWARDED_ZONE_ID", "\"$tapsellRewardedZoneId\"")
        buildConfigField("String", "TAPSELL_BANNER_ZONE_ID", "\"$tapsellBannerZoneId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Image loading & Networking
    implementation(libs.coil.compose)
    implementation(libs.okhttp)

    // Tapsell Plus SDK
    implementation(libs.tapsell.plus.sdk)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
