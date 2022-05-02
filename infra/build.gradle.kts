plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("realm-android")
}

android {
    compileSdk = Dep.compileSdkVersion

    defaultConfig {
        minSdk = Dep.minSdkVersion
        targetSdk = Dep.targetSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Dep.Hilt.android)
    kapt(Dep.Hilt.compiler)
    implementation(Dep.Retrofit.retrofit)
    implementation(Dep.Retrofit.converter)
    implementation(Dep.OkHttp.okHttp)
    implementation(Dep.Moshi.moshi)
    implementation(Dep.Moshi.moshiKotlin)
    kapt(Dep.Moshi.moshiCodegen)
    implementation(Dep.Coroutine.core)
    implementation(Dep.AndroidX.preference)

    implementation(project(":domain"))
}