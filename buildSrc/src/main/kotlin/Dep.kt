object Dep {
    const val compileSdkVersion = 31
    const val minSdkVersion = 23
    const val targetSdkVersion = 30

    const val kotlinVersion = "1.5.10"
    const val fragmentVersion = "1.3.2"
    const val navVersion = "2.3.3"
    const val hiltVersion = "2.41"
    const val retrofitVersion = "2.9.0"
    const val moshiVersion = "1.9.2"
    const val composeVersion = "1.0.5"
    const val coroutineVersion = "1.6.1"
    const val lifeCycleVersion = "2.4.1"

    object AndroidX {
        const val lifeCycleViewModel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion"
    }

    object Compose {
        const val ui = "androidx.compose.ui:ui:$composeVersion"
        const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
        const val material = "androidx.compose.material:material:$composeVersion"
        const val materialIconsCore =
            "androidx.compose.material:material-icons-core:$composeVersion"
        const val materialIconsExtended =
            "androidx.compose.material:material-icons-extended:$composeVersion"
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
}