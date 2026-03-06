import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.service)
    alias(libs.plugins.crashlitics)
    alias(libs.plugins.kotlinx.serialization)
}

val versionMajor = 1
val versionMinor = 1 //max 9
val versionPatch = 2 //max 9
val versionBuild = 5 //max 99

val props = loadLocalProperties(rootProject.file("local.properties"))

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file(props["debug.storeFile"] as String)
            storePassword = props["debug.storePassword"] as String
            keyAlias = props["debug.keyAlias"] as String
            keyPassword = props["debug.keyPassword"] as String
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

        //Setup Maps props
        buildConfigField("String", "mapsApiKey", "\"${props["maps.apiKey"] as String}\"")
        manifestPlaceholders["mapsApiKey"] = props["maps.apiKey"] as String
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    //Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.kotlinx.datetime)

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
    implementation(libs.androidx.compose.ui.unit)


    //Time Formater
    implementation(libs.prettytime)

    //Navigation
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.transitions)
    implementation(libs.voyager.screenmodel)

    //Chrome Tabs
    implementation(libs.androidx.browser)

    //Koin DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    //In-app updates
    implementation(libs.app.update.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    //Permissions
    implementation(libs.accompanist.permissions)

    //Link Preview
    implementation(libs.iquack.link.preview)

    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.play.services.location)


    // Phone number utils
    implementation(libs.libphonenumber)

    //Serialization
    implementation(libs.kotlinx.serialization.json)

    //WebView
    implementation(libs.androidx.webkit)

    //Firebase
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.analytics)

    //Testing
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(project(":app:service"))
    implementation(project(":app:data"))

}

fun loadLocalProperties(file: File): Properties {
    val properties = Properties()
    if (file.exists()) {
        file.inputStream().use { properties.load(it) }
    } else {
        throw GradleException("local.properties not found!")
    }
    return properties
}