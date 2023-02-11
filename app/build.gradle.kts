plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("realm-android")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.github.konifar.gradle.unused-resources-remover")
    id("androidx.navigation.safeargs.kotlin")
    id("co.uzzu.dotenv.gradle")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(env.TESTMAKER_STORE_PATH.value)
            keyAlias = env.TESTMAKER_KEY_ALIAS.value
            storePassword = env.TESTMAKER_KEY_STORE_PASS.value
            keyPassword = env.TESTMAKER_KEY_PASS.value
        }
    }
    compileSdk = Dep.compileSdkVersion

    defaultConfig {
        applicationId = "jp.gr.java_conf.foobar.testmaker.service"
        minSdk = Dep.minSdkVersion
        targetSdk = Dep.targetSdkVersion
        multiDexEnabled = true
        versionCode = 191
        versionName = "6.0.14"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    packagingOptions {
        resources {
            excludes.add("META-INF/atomicfu.kotlin_module")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["studyplus_consumer_key"] = env.STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["secret_studyplus_consumer_key"] =
                env.SECRET_STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["testmaker_admob_key"] = env.TESTMAKER_ADMOB_KEY.value
            manifestPlaceholders["testmaker_admob_rewarded_key"] =
                env.TESTMAKER_ADMOB_REWARDED_KEY.value
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            manifestPlaceholders["studyplus_consumer_key"] = env.STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["secret_studyplus_consumer_key"] =
                env.SECRET_STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["testmaker_admob_key"] = env.TESTMAKER_ADMOB_KEY.value
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions("pay")

    productFlavors {
        create("normal") {
            dimension = "pay"
        }
        create("premium") {
            dimension = "pay"
            applicationIdSuffix = ".premium"
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    kapt {
        correctErrorTypes = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Dep.composeCompilerVersion
    }
}

dependencies {
    implementation(Dep.Hilt.android)
    kapt(Dep.Hilt.compiler)
    implementation(Dep.AndroidX.fragment)
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation(Dep.Material.core)
    implementation(Dep.Compose.ui)
    implementation(Dep.Compose.foundation)
    implementation(Dep.Compose.material)
    implementation(Dep.Compose.materialIconsCore)
    implementation(Dep.Compose.materialIconsExtended)
    implementation("androidx.compose.runtime:runtime-livedata:1.0.5")
    implementation(Dep.Compose.activity)
    implementation("com.google.accompanist:accompanist-swiperefresh:0.23.1")
    implementation("com.google.accompanist:accompanist-pager:0.19.0")
    implementation(Dep.PlayServices.ads)
    implementation("com.google.android.play:core-ktx:1.8.0")
    implementation(Dep.Misc.studyPlus)
    implementation(platform(Dep.Firebase.bom))
    implementation(Dep.Firebase.core)
    implementation(Dep.Firebase.storage)
    implementation(Dep.Firebase.dynamicLinks)
    implementation(Dep.Firebase.analytics)
    implementation(Dep.Firebase.crashlytics)
    implementation(Dep.Firebase.authUi)
    implementation(Dep.Firebase.storageUi)
    implementation(Dep.Firebase.fireStoreUi)
    implementation(Dep.Coroutine.android)
    implementation(Dep.Coroutine.core)
    implementation(Dep.Coroutine.playServices)
    implementation(Dep.Glide.core)
    kapt(Dep.Glide.compiler)
    implementation(Dep.AndroidX.preference)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation(Dep.AndroidX.lifeCycleViewModel)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Dep.kotlinVersion}")
    implementation("androidx.navigation:navigation-fragment-ktx:2.2.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.2.2")
    api(Dep.BillingClient.billing)
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation(Dep.Test.jUnit4)
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("tools.fastlane:screengrab:1.0.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.0")
    androidTestImplementation("com.jraska:falcon:2.2.0")

    implementation(project(":ui"))
    implementation(project(":domain"))
    implementation(project(":usecase")) // todo モジュール分割中の一時的な処置を削除する（フル Compose にした後）
    implementation(project(":infra"))
    implementation(project(":core"))
}
repositories {
    mavenCentral()
}
