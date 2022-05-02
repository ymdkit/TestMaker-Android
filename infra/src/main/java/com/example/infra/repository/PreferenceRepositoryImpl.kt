package com.example.infra.repository

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


    override fun putIsRemovedAd(isRemovedAd: Boolean) {
        preference.isRemovedAd = isRemovedAd
        _updateIsRemovedAdFlow.tryEmit(isRemovedAd)
    }

    override fun isRemovedAd(): Boolean =
        preference.isRemovedAd
}