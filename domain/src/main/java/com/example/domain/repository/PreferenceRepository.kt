package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val updateIsRemovedAdFlow: Flow<Boolean>

    fun putIsRemovedAd(isRemovedAd: Boolean)
    fun isRemovedAd(): Boolean
}