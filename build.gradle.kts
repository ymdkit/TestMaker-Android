// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        google()
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("io.realm:realm-gradle-plugin:6.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.2")
        classpath("gradle.plugin.com.github.konifar.gradle:plugin:0.3.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Deps.navVersion}")
        classpath("co.uzzu.dotenv:gradle:1.1.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        maven("https://jitpack.io")
    }
}
