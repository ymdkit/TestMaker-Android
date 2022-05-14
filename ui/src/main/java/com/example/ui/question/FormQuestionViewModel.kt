package com.example.ui.question

import com.example.core.QuestionImage
import com.example.core.QuestionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface FormQuestionViewModel {

    val uiState: StateFlow<FormQuestionUiState>

    fun onQuestionTypeChanged(value: QuestionType): Job
    fun onProblemChanged(value: String): Job
    fun onAnswerListChanged(value: List<String>): Job
    fun onOtherSelectionListChanged(value: List<String>): Job
    fun onExplanationChanged(value: String): Job
    fun onProblemImageUrlChanged(value: QuestionImage): Job
    fun onExplanationImageUrlChanged(value: QuestionImage): Job
    fun onIsAutoGenerateOtherSelectionsChanged(value: Boolean): Job
    fun onIsCheckAnswerOrderChanged(value: Boolean): Job
}
