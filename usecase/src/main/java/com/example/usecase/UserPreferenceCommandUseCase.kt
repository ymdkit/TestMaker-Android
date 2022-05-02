package com.example.usecase

import com.example.domain.repository.PreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferenceCommandUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {

    fun putIsRemovedAd(isRemovedAd: Boolean) =
        preferenceRepository.putIsRemovedAd(isRemovedAd)

}
