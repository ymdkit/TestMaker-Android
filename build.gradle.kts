// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("io.realm:realm-gradle-plugin:10.13.2-transformer-api")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Dep.kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.5")
        classpath("gradle.plugin.com.github.konifar.gradle:plugin:0.3.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Dep.navVersion}")
        classpath("co.uzzu.dotenv:gradle:1.1.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Dep.hiltVersion}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.android.application") version "7.1.2" apply false
    id("com.android.library") version "7.1.2" apply false
    id("org.jetbrains.kotlin.android") version Dep.kotlinVersion apply false
    id("org.jetbrains.kotlin.kapt") version Dep.kotlinVersion apply false
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
}
