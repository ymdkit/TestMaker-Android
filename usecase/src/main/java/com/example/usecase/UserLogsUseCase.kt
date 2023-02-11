package com.example.usecase

import com.example.domain.repository.UserLogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLogsUseCase @Inject constructor(
    private val userLogRepository: UserLogRepository
) {
    suspend fun uploadUserLogs() = userLogRepository.uploadUserLog()
}