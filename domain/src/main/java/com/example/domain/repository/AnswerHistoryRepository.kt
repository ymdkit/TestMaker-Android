package com.example.domain.repository

import com.example.domain.model.AnswerHistory
import com.example.domain.model.DocumentId
import com.example.domain.model.User

interface AnswerHistoryRepository {
    suspend fun getAnswerHistoryList(workbookId: DocumentId): List<AnswerHistory>
    suspend fun createHistory(workbookId: DocumentId, user: User, numCorrect: Int, numSolved: Int)
}