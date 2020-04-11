package jp.gr.java_conf.foobar.testmaker.service.infra.api

import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CloudFunctionsService {

    @FormUrlEncoded
    @POST("textToTest")
    suspend fun textToTest(@Field("text") text: String, @Field("lang") lang: String): Test

}