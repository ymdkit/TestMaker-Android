package com.example.infra.repository

import com.example.domain.model.AnswerHistory
import com.example.domain.model.DocumentId
import com.example.domain.model.User
import com.example.domain.repository.AnswerHistoryRepository
import com.example.infra.remote.entity.FirebaseHistory
import com.example.infra.repository.SharedWorkbookRepositoryImpl.Companion.WORKBOOK_COLLECTION_NAME
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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

    override suspend fun createHistory(
        workbookId: DocumentId,
        user: User,
        numCorrect: Int,
        numSolved: Int
    ) {

        val ref = db.collection(WORKBOOK_COLLECTION_NAME)
            .document(workbookId.value)
            .collection("histories")
            .document()

        val newAnswerHistory =
            AnswerHistory(
                id = DocumentId(value = ref.id),
                userId = user.id,
                userName = user.displayName,
                createdAt = "",
                numCorrect = numCorrect,
                numSolved = numSolved
            )

        ref.set(FirebaseHistory.fromHistory(newAnswerHistory)).await()
    }
}