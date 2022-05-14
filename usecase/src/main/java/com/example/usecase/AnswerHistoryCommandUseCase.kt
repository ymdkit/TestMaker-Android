package com.example.usecase

import com.example.domain.model.DocumentId
import com.example.domain.repository.AnswerHistoryRepository
import com.example.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerHistoryCommandUseCase @Inject constructor(
    private val repository: AnswerHistoryRepository,
    private val userRepository: UserRepository
) {

    suspend fun createHistory(workbookId: String, numCorrect: Int, numSolved: Int) {
        val user = userRepository.getUserOrNull() ?: return
        repository.createHistory(
            workbookId = DocumentId(value = workbookId),
            user = user,
            numCorrect = numCorrect,
            numSolved = numSolved
        )
    }
}
