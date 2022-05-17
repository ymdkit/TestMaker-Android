package com.example.usecase.model

import com.example.core.QuestionCondition
import com.example.domain.model.AnswerSetting

data class AnswerSettingUseCaseModel(
    val isRandomOrder: Boolean,
    val isSwapProblemAndAnswer: Boolean,
    val questionCondition: QuestionCondition,
    val isSelfScoring: Boolean,
    val isAlwaysShowExplanation: Boolean,
    val isPlaySound: Boolean,
    val isCaseInsensitive: Boolean,
    val isShowAnswerSettingDialog: Boolean,
    val questionCount: Int,
    val startPosition: Int,
) {
    companion object {
        fun fromAnswerSetting(setting: AnswerSetting) =
            AnswerSettingUseCaseModel(
                isRandomOrder = setting.isRandomOrder,
                isSwapProblemAndAnswer = setting.isSwapProblemAndAnswer,
                questionCondition = setting.questionCondition,
                isSelfScoring = setting.isSelfScoring,
                isAlwaysShowExplanation = setting.isAlwaysShowExplanation,
                isPlaySound = setting.isPlaySound,
                isShowAnswerSettingDialog = setting.isShowAnswerSettingDialog,
                isCaseInsensitive = setting.isCaseInsensitive,
                questionCount = setting.questionCount.coerceAtLeast(1),
                startPosition = setting.startPosition.coerceAtLeast(1)
            )
    }

    fun toAnswerSetting() =
        AnswerSetting(
            isRandomOrder = isRandomOrder,
            isSwapProblemAndAnswer = isSwapProblemAndAnswer,
            questionCondition = questionCondition,
            isSelfScoring = isSelfScoring,
            isAlwaysShowExplanation = isAlwaysShowExplanation,
            isPlaySound = isPlaySound,
            isCaseInsensitive = isCaseInsensitive,
            isShowAnswerSettingDialog = isShowAnswerSettingDialog,
            questionCount = questionCount.coerceAtLeast(1),
            startPosition = startPosition.coerceAtLeast(1)
        )
}