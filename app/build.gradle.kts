plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.service)
    alias(libs.plugins.crashlitics)
}

val versionMajor = 1
val versionMinor = 0 //max 9
val versionPatch = 0 //max 9
val versionBuild = 1 //max 99

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("/home/duckrya/Projects/Android/keystores/debug.keystore")
            keyAlias = "androiddebugkey"
            storePassword = "android"
            keyPassword = "android"
        }
    }
    namespace = "net.alienminds.ethnogram"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.alienminds.ethnogram"
        minSdk = 26
        targetSdk = 36
        versionCode = versionMajor * 10000 + versionMinor * 1000 + versionPatch * 100 + versionBuild
        versionName = "${versionMajor}.${versionMinor}.${versionPatch} ($versionCode)"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    //Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.compose.shimmer)
    implementation(libs.cloudy)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material.icons.extended)

    //Time Formater
    implementation(libs.prettytime)

    //Navigation
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.transitions)
    implementation(libs.voyager.screenmodel)

    //Chrome Tabs
    implementation(libs.androidx.browser)
    implementation(project(":app:service"))

    //Koin DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    //In-app updates
    implementation(libs.app.update.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    //Permissions
    implementation(libs.accompanist.permissions)

    implementation(libs.iquack.link.preview)

    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.analytics)
    debugImplementation(libs.androidx.compose.ui.tooling)

}