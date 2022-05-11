package com.example.infra.repository

import com.example.domain.model.DocumentId
import com.example.domain.model.SharedWorkbook
import com.example.domain.model.UserId
import com.example.domain.repository.SharedWorkbookRepository
import com.example.infra.remote.entity.FirebaseTest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SharedWorkbookRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : SharedWorkbookRepository {

    companion object {
        const val COLLECTION_NAME = "tests"
    }

    private val _updateWorkbookListFlow: MutableSharedFlow<List<SharedWorkbook>> =
        MutableSharedFlow()
    override val updateWorkBookListFlow: Flow<List<SharedWorkbook>>
        get() = _updateWorkbookListFlow

    override suspend fun getWorkbookListByUserId(userId: UserId): List<SharedWorkbook> {
        val documents = db.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(300)
            .get()
            .await()
            .documents

        return documents.map { it.toObject(FirebaseTest::class.java)!!.toSharedWorkbook(it.id) }
    }

    override suspend fun createWorkbook(workbook: SharedWorkbook) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWorkbook(workbookId: DocumentId) {
        TODO("Not yet implemented")
    }
}
