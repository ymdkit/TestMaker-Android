plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
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
    buildFeatures {
        dataBinding = true
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Dep.composeVersion
    }
}

dependencies {
    implementation(Dep.Hilt.android)
    kapt(Dep.Hilt.compiler)
    implementation(Dep.AndroidX.lifeCycleViewModel)
    implementation(Dep.Compose.ui)
    implementation(Dep.Compose.foundation)
    implementation(Dep.Compose.material)
    implementation(Dep.Compose.materialIconsCore)
    implementation(Dep.Compose.materialIconsExtended)
    implementation(Dep.Compose.activity)
    implementation(Dep.Epoxy.core)
    implementation(Dep.Epoxy.dataBinding)
    kapt(Dep.Epoxy.processor)
    implementation(Dep.PlayServices.ads)
    api(Dep.BillingClient.billing)
    implementation(Dep.Material.core)
    api(Dep.Misc.cropView)
    implementation(Dep.Misc.photoView)
    implementation(platform(Dep.Firebase.bom))
    implementation(Dep.Firebase.storage)
    implementation(Dep.Firebase.storageUi)
    implementation(Dep.Glide.core)
    kapt(Dep.Glide.compiler)
    implementation(project(":usecase"))
    implementation(project(":core"))
}