package com.example.infra.repository

import com.example.domain.model.AnswerHistory
import com.example.domain.model.DocumentId
import com.example.domain.repository.AnswerHistoryRepository
import com.example.infra.remote.entity.FirebaseHistory
import com.example.infra.repository.SharedWorkbookRepositoryImpl.Companion.WORKBOOK_COLLECTION_NAME
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AnswerHistoryRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AnswerHistoryRepository {

    companion object {
        const val HISTORY_COLLECTION_NAME = "histories"
    }

    override suspend fun getAnswerHistoryList(workbookId: DocumentId): List<AnswerHistory> =
        db.collection(WORKBOOK_COLLECTION_NAME)
            .document(workbookId.value)
            .collection(HISTORY_COLLECTION_NAME)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .toObjects(FirebaseHistory::class.java)
            .map { it.toAnswerHistory() }
}