package com.example.usecase

import com.example.core.TestMakerColor
import com.example.domain.repository.PreferenceRepository
import com.example.usecase.model.AnswerSettingUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferenceCommandUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {

    suspend fun putIsRemovedAd(isRemovedAd: Boolean) =
        preferenceRepository.putIsRemovedAd(isRemovedAd)

    suspend fun putAnswerSetting(answerSetting: AnswerSettingUseCaseModel) =
        preferenceRepository.putAnswerSetting(answerSetting.toAnswerSetting())

    suspend fun putThemeColor(themeColor: TestMakerColor) =
        preferenceRepository.putThemeColor(themeColor)

}
