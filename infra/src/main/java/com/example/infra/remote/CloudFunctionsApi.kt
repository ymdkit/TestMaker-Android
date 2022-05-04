package com.example.infra.remote

import com.example.domain.model.Question
import com.example.domain.model.QuestionType
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
) {
    companion object {
        fun fromWorkbook(workbook: Workbook) =
            ExportWorkbookRequest(
                id = workbook.id.value,
                color = workbook.color,
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
            when (question) {
                is Question.WriteQuestion -> ExportQuestionRequest(
                    id = question.id.value,
                    type = QuestionType.WRITE.value,
                    question = question.problem,
                    answer = question.answers.firstOrNull() ?: "",
                    answers = question.answers,
                    explanation = question.explanation,
                    imagePath = question.problemImageUrl,
                    others = listOf(),
                    order = question.order,
                    isAutoGenerateOthers = false,
                    isCheckOrder = false
                )
                is Question.SelectQuestion -> ExportQuestionRequest(
                    id = question.id.value,
                    type = QuestionType.SELECT.value,
                    question = question.problem,
                    answer = question.answers.firstOrNull() ?: "",
                    answers = question.answers,
                    explanation = question.explanation,
                    imagePath = question.problemImageUrl,
                    others = question.otherSelections,
                    order = question.order,
                    isAutoGenerateOthers = question.isAutoGenerateOtherSelections,
                    isCheckOrder = false
                )
                is Question.CompleteQuestion -> ExportQuestionRequest(
                    id = question.id.value,
                    type = QuestionType.COMPLETE.value,
                    question = question.problem,
                    answer = "",
                    answers = question.answers,
                    explanation = question.explanation,
                    imagePath = question.problemImageUrl,
                    others = listOf(),
                    order = question.order,
                    isAutoGenerateOthers = false,
                    isCheckOrder = question.isCheckAnswerOrder
                )
                is Question.SelectCompleteQuestion -> ExportQuestionRequest(
                    id = question.id.value,
                    type = QuestionType.SELECT_COMPLETE.value,
                    question = question.problem,
                    answer = "",
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