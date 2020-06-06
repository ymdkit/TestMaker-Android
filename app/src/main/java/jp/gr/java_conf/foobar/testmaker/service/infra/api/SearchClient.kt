package jp.gr.java_conf.foobar.testmaker.service.infra.api

import com.google.firebase.Timestamp
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jp.gr.java_conf.foobar.testmaker.service.di.TimestampJsonAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SearchClient {

    private val retrofit = Retrofit.Builder()
            .baseUrl("https://test-maker-server.herokuapp.com")
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                    .add(Timestamp::class.java, TimestampJsonAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .build()))
            .build()

    fun create(): SearchService {
        return retrofit.create(SearchService::class.java)
    }
}