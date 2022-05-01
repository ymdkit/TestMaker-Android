package com.example.infra.remote

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CloudFunctionsApi {

    @FormUrlEncoded
    @POST("textToTest")
    suspend fun textToTest(
        @Field("title") title: String,
        @Field("text") text: String,
        @Field("lang") lang: String
    ): ImportWorkbookResponse

    @POST("testToText")
    suspend fun testToText(@Body workbook: ExportWorkbookRequest): CSVTest

}

data class ExportWorkbookRequest(
    val id: Long,
    val color: Int,
    val title: String,
    val lang: String,
    val questions: List<ExportQuestionRequest>
)

data class ExportQuestionRequest(
    val id: Long,
    val question: String,
    val answer: String,
    val explanation: String,
    val imagePath: String,
    val answers: List<String>,
    val others: List<String>,
    val type: Int,
    val isAutoGenerateOthers: Boolean,
    val order: Int,
    val isCheckOrder: Boolean
)

data class CSVTest(val text: String)

data class ImportWorkbookResponse(
    val title: String,
    val lang: String,
    val questions: List<ImportQuestionResponse>
)

data class ImportQuestionResponse(
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