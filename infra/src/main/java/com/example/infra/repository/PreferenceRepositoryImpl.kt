package com.example.infra.repository

import com.example.core.QuestionCondition
import com.example.core.TestMakerColor
import com.example.domain.model.AnswerSetting
import com.example.domain.repository.PreferenceRepository
import com.example.infra.local.preference.PreferenceDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val preference: PreferenceDataSource
) : PreferenceRepository {

    private val _updateIsRemovedAdFlow: MutableSharedFlow<Boolean> =
        MutableSharedFlow()
    override val updateIsRemovedAdFlow: Flow<Boolean>
        get() = _updateIsRemovedAdFlow

    private val _updateAnswerSettingFlow: MutableSharedFlow<AnswerSetting> =
        MutableSharedFlow()
    override val updateAnswerSettingFlow: Flow<AnswerSetting>
        get() = _updateAnswerSettingFlow

    private val _updateThemeColorFlow: MutableSharedFlow<TestMakerColor> =
        MutableSharedFlow()
    override val updateThemeColorFlow: Flow<TestMakerColor>
        get() = _updateThemeColorFlow

    override suspend fun putIsRemovedAd(isRemovedAd: Boolean) {
        preference.isRemovedAd = isRemovedAd
        _updateIsRemovedAdFlow.emit(isRemovedAd)
    }

    override fun isRemovedAd(): Boolean =
        preference.isRemovedAd

    override fun getAnswerSetting(): AnswerSetting =
        AnswerSetting(
            isRandomOrder = preference.random,
            isSwapProblemAndAnswer = preference.isSwapProblemAndAnswer,
            questionCondition = if (preference.refine) QuestionCondition.WRONG else QuestionCondition.ALL,
            isSelfScoring = preference.isSelfScoring,
            isAlwaysShowExplanation = preference.isAlwaysShowExplanation,
            isPlaySound = preference.isPlaySound,
            isCaseInsensitive = preference.isCaseInsensitive,
            isShowAnswerSettingDialog = preference.isShowAnswerSettingDialog,
            questionCount = preference.questionCount,
            startPosition = preference.startPosition
        )

    override suspend fun putAnswerSetting(answerSetting: AnswerSetting) {
        preference.random = answerSetting.isRandomOrder
        preference.isSwapProblemAndAnswer = answerSetting.isSwapProblemAndAnswer
        preference.refine = answerSetting.questionCondition == QuestionCondition.WRONG
        preference.isSelfScoring = answerSetting.isSelfScoring
        preference.isAlwaysShowExplanation = answerSetting.isAlwaysShowExplanation
        preference.isPlaySound = answerSetting.isPlaySound
        preference.isCaseInsensitive = answerSetting.isCaseInsensitive
        preference.isShowAnswerSettingDialog = answerSetting.isShowAnswerSettingDialog
        preference.questionCount = answerSetting.questionCount
        preference.startPosition = answerSetting.startPosition
        _updateAnswerSettingFlow.emit(answerSetting)
    }

    override suspend fun putThemeColor(color: TestMakerColor) {
        preference.themeColor = color.name
        _updateThemeColorFlow.emit(color)
    }

    override fun getThemeColor(): TestMakerColor =
        TestMakerColor.values().firstOrNull { it.name == preference.themeColor }
            ?: TestMakerColor.BLUE
}