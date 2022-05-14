package com.example.domain.repository

import com.example.domain.model.AnswerHistory
import com.example.domain.model.DocumentId

interface AnswerHistoryRepository {
    suspend fun getAnswerHistoryList(workbookId: DocumentId): List<AnswerHistory>
}