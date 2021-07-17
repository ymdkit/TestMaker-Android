package jp.gr.java_conf.foobar.testmaker.service.infra.api

import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CloudFunctionsService {

    @FormUrlEncoded
    @POST("textToTest")
    suspend fun textToTest(@Field("title") title: String = "no title", @Field("text") text: String, @Field("lang") lang: String): TestResponse

    @POST("testToText")
    suspend fun testToText(@Body test: Test): CSVTest

}

data class CSVTest(val text: String)

data class TestResponse(
    val title: String,
    val lang: String,
    val questions: List<QuestionResponse>
)

data class QuestionResponse(
    val question: String,
    val answer: String,
    val explanation: String,
    val answers: List<String>,
    val others: List<String>,
    val type: Int,
    val isAutoGenerateOthers: Boolean,
    val order: Int,
    val isCheckOrder: Boolean,
    val imagePath: String
)