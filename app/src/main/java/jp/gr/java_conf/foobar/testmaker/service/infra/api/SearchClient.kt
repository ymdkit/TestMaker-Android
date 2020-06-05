package jp.gr.java_conf.foobar.testmaker.service.infra.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SearchClient {

    private val retrofit = Retrofit.Builder()
            .baseUrl("https://test-maker-server.herokuapp.com")
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()))
            .build()

    fun create(): SearchService {
        return retrofit.create(SearchService::class.java)
    }
}