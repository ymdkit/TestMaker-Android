package com.example.infra.remote

import com.example.domain.model.Question
import com.example.domain.model.Workbook
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*

interface CloudFunctionsApi {

    @FormUrlEncoded
    @POST("textToTest")
    suspend fun textToTest(
        @Field("title") workbookName: String,
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
) {
    companion object {
        fun fromWorkbook(workbook: Workbook) =
            ExportWorkbookRequest(
                id = workbook.id.value,
                color = 0, // todo
                title = workbook.name,
                lang = if (Locale.getDefault().language == "ja") "ja" else "en",
                questions = workbook.questionList.map { ExportQuestionRequest.fromQuestion(it) }
            )
    }
}

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
) {
    companion object {
        fun fromQuestion(question: Question) =
            ExportQuestionRequest(
                id = question.id.value,
                type = question.type.value,
                question = question.problem,
                answer = question.answers.firstOrNull() ?: "",
                answers = question.answers,
                explanation = question.explanation,
                imagePath = question.problemImageUrl,
                others = question.otherSelections,
                order = question.order,
                isAutoGenerateOthers = question.isAutoGenerateOtherSelections,
                isCheckOrder = question.isCheckAnswerOrder
            )
    }
}

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