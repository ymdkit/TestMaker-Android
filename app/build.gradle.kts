plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
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
    compileSdkVersion(Deps.compileSdkVersion)
    buildToolsVersion = "29.0.2"

    defaultConfig {
        applicationId = "jp.gr.java_conf.foobar.testmaker.service"
        minSdkVersion(Deps.minSdkVersion)
        targetSdkVersion(Deps.targetSdkVersion)
        multiDexEnabled = true
        versionCode = 139
        versionName = "4.0.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["studyplus_consumer_key"] = env.STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["secret_studyplus_consumer_key"] = env.SECRET_STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["testmaker_admob_key"] = env.TESTMAKER_ADMOB_KEY.value
            manifestPlaceholders["testmaker_admob_rewarded_key"] = env.TESTMAKER_ADMOB_REWARDED_KEY.value
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            manifestPlaceholders["studyplus_consumer_key"] = env.STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["secret_studyplus_consumer_key"] = env.SECRET_STUDYPLUS_CONSUMER_KEY.value
            manifestPlaceholders["testmaker_admob_key"] = env.TESTMAKER_ADMOB_KEY.value
            setApplicationIdSuffix(".debug")
        }
    }

    flavorDimensions("pay")

    productFlavors {
        create("normal") {
            dimension("pay")
        }
        create("premium") {
            dimension("pay")
            setApplicationIdSuffix(".premium")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    buildFeatures {
        dataBinding = true
    }

    kapt {
        correctErrorTypes = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.gms:play-services-ads:19.3.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.android.play:core-ktx:1.8.0")
    implementation("com.github.studyplus:Studyplus-Android-SDK:2.6.0")
    implementation(platform("com.google.firebase:firebase-bom:26.2.0"))
    implementation("com.google.firebase:firebase-core")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-dynamic-links")
    implementation("com.google.firebase:firebase-dynamic-links-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.firebaseui:firebase-ui-auth:4.3.1")
    implementation("com.firebaseui:firebase-ui-storage:4.1.0")
    implementation("com.firebaseui:firebase-ui-firestore:5.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.5.0-native-mt")
    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation("org.koin:koin-core:1.0.2")
    implementation("org.koin:koin-android:1.0.2")
    implementation("org.koin:koin-java:1.0.2")
    implementation("org.koin:koin-androidx-viewmodel:1.0.2")
    implementation("androidx.paging:paging-runtime:2.1.2")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("com.airbnb.android:epoxy:3.11.0")
    implementation("com.airbnb.android:epoxy-databinding:3.11.0")
    implementation("com.github.chrisbanes:PhotoView:2.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.2.0")
    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlinVersion}")
    implementation("androidx.navigation:navigation-fragment-ktx:2.2.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.2.2")
    implementation("androidx.fragment:fragment-ktx:${Deps.fragmentVersion}")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")
    kapt("com.airbnb.android:epoxy-processor:3.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.9.0")
    kapt("com.github.bumptech.glide:compiler:4.9.0")
    api("com.isseiaoki:simplecropview:1.1.4")
    api("androidx.multidex:multidex:2.0.1")
    api("com.android.billingclient:billing:3.0.2")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("tools.fastlane:screengrab:1.0.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("com.jraska:falcon:2.1.1")
}
repositories {
    mavenCentral()
}
