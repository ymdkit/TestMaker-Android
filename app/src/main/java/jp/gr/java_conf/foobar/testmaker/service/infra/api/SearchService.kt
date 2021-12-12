package jp.gr.java_conf.foobar.testmaker.service.infra.api

import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import retrofit2.http.*

interface SearchService {
    @GET("tests")
    suspend fun tests(@Query("query") keyword: String): List<FirebaseTest>

    @FormUrlEncoded
    @PUT("tests/{documentId}")
    suspend fun updateTest(
        @Path("documentId") documentId: String,
        @Field("size") size: Int,
        @Field("download_count") downloadCount: Int
    )
}