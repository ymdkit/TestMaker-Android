package jp.gr.java_conf.foobar.testmaker.service.infra.api

import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {
    @GET("tests")
    suspend fun tests(@Query("query") keyword: String): List<FirebaseTest>
}