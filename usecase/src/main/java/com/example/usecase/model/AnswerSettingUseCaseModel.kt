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
                questionCount = setting.questionCount,
                startPosition = setting.startPosition
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
            isShowAnswerSettingDialog = isShowAnswerSettingDialog,
            questionCount = questionCount,
            startPosition = startPosition
        )
}