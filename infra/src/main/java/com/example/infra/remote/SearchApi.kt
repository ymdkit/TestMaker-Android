package com.example.infra.remote

import com.squareup.moshi.Json
import retrofit2.http.*

interface SearchApi {
    @GET("tests")
    suspend fun tests(@Query("query") keyword: String): List<SearchWorkbookResponse>

    @FormUrlEncoded
    @PUT("tests/{documentId}")
    suspend fun updateTest(
        @Path("documentId") documentId: String,
        @Field("size") size: Int,
        @Field("download_count") downloadCount: Int
    )
}

data class SearchWorkbookResponse(
    val name: String,
    val color: Int,
    @Json(name = "document_id") val documentId: String,
    val size: Int,
    val comment: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String,
    @Json(name = "created_at") val createdAt: TimeStampResponse,
    @Json(name = "updated_at") val updatedAt: TimeStampResponse
)

data class TimeStampResponse(
    @Json(name = "secs_since_epoch") val secs: Long,
)