object Dep {
    const val compileSdkVersion = 32
    const val minSdkVersion = 23
    const val targetSdkVersion = 32

    const val kotlinVersion = "1.7.0"
    const val fragmentVersion = "1.3.2"
    const val navVersion = "2.3.3"
    const val hiltVersion = "2.42"
    const val retrofitVersion = "2.9.0"
    const val moshiVersion = "1.9.2"
    const val composeVersion = "1.2.0-rc03"
    const val composeCompilerVersion = "1.2.0"
    const val coroutineVersion = "1.6.1"
    const val lifeCycleVersion = "2.4.1"
    const val glideVersion = "4.13.0"

    object AndroidX {
        const val lifeCycleViewModel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion"
        const val preference = "androidx.preference:preference-ktx:1.2.0"
        const val fragment = "androidx.fragment:fragment-ktx:$fragmentVersion"
    }

    object Compose {
        const val ui = "androidx.compose.ui:ui:$composeVersion"
        const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
        const val material = "androidx.compose.material:material:$composeVersion"
        const val materialIconsCore =
            "androidx.compose.material:material-icons-core:$composeVersion"
        const val materialIconsExtended =
            "androidx.compose.material:material-icons-extended:$composeVersion"
        const val activity = "androidx.activity:activity-compose:1.4.0"
        const val systemUiController =
            "com.google.accompanist:accompanist-systemuicontroller:0.24.8-beta"
    }

    object Material {
        const val core = "com.google.android.material:material:1.5.0"
    }

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:30.5.0"
        const val core = "com.google.firebase:firebase-core"
        const val storage = "com.google.firebase:firebase-storage"
        const val auth = "com.google.firebase:firebase-auth-ktx"
        const val fireStore = "com.google.firebase:firebase-firestore"
        const val dynamicLinks = "com.google.firebase:firebase-dynamic-links-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics"
        const val authUi = "com.firebaseui:firebase-ui-auth:7.2.0"
        const val storageUi = "com.firebaseui:firebase-ui-storage:4.1.0"
        const val fireStoreUi = "com.firebaseui:firebase-ui-firestore:5.0.0"
    }

    object Glide {
        const val core = "com.github.bumptech.glide:glide:$glideVersion"
        const val compiler = "com.github.bumptech.glide:compiler:$glideVersion"
    }

    object PlayServices {
        const val ads = "com.google.android.gms:play-services-ads:21.0.0"
        const val oss = "com.google.android.gms:play-services-oss-licenses:17.0.0"
    }

    object BillingClient {
        const val billing = "com.android.billingclient:billing-ktx:4.1.0"
    }

    object Hilt {
        const val android = "com.google.dagger:hilt-android:$hiltVersion"
        const val compiler = "com.google.dagger:hilt-compiler:$hiltVersion"
    }

    object Retrofit {
        const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
        const val converter = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"
    }

    object OkHttp {
        const val okHttp = "com.squareup.okhttp3:okhttp:4.9.3"
    }

    object Moshi {
        const val moshi = "com.squareup.moshi:moshi:$moshiVersion"
        const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:$moshiVersion"
        const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
    }

    object Coroutine {
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion"
        const val playServices =
            "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutineVersion"
    }

    object Misc {
        const val cropView = "com.isseiaoki:simplecropview:1.1.4"
        const val photoView = "com.github.chrisbanes:PhotoView:2.0.0"
        const val studyPlus = "com.github.studyplus:Studyplus-Android-SDK:3.0.0"
    }

    object Test {
        const val jUnit4 = "junit:junit:4.13.2"
        const val mockK = "io.mockk:mockk:1.12.4"
    }

}