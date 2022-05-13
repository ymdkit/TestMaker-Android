package com.example.infra.repository

import android.net.Uri
import com.example.domain.model.*
import com.example.domain.repository.SharedWorkbookRepository
import com.example.infra.remote.DynamicLinksCreator
import com.example.infra.remote.SearchApi
import com.example.infra.remote.SearchClient
import com.example.infra.remote.entity.FirebaseQuestion
import com.example.infra.remote.entity.FirebaseTest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SharedWorkbookRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    @SearchClient private val searchApi: SearchApi,
    private val dynamicLinksCreator: DynamicLinksCreator
) : SharedWorkbookRepository {

    companion object {
        const val WORKBOOK_COLLECTION_NAME = "tests"
        const val QUESTION_COLLECTION_NAME = "questions"
    }

    private val _updateWorkbookListFlow: MutableSharedFlow<List<SharedWorkbook>> =
        MutableSharedFlow()
    override val updateWorkbookListFlow: Flow<List<SharedWorkbook>>
        get() = _updateWorkbookListFlow

    private val _updateGroupWorkbookListFlow: MutableSharedFlow<List<SharedWorkbook>> =
        MutableSharedFlow()
    override val updateGroupWorkbookListFlow: Flow<List<SharedWorkbook>>
        get() = _updateGroupWorkbookListFlow

    override suspend fun getWorkbookList(query: String): List<SharedWorkbook> =
        searchApi.tests(keyword = query).map { it.toSharedWorkbook() }

    override suspend fun getWorkbookListByUserId(userId: UserId): List<SharedWorkbook> {
        val documents = db.collection(WORKBOOK_COLLECTION_NAME)
            .whereEqualTo("userId", userId.value)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(300)
            .get()
            .await()

        return documents.map { it.toObject(FirebaseTest::class.java).toSharedWorkbook(it.id) }
    }

    override suspend fun getWorkbookListByGroupId(groupId: GroupId): List<SharedWorkbook> {
        val documents = db.collection(WORKBOOK_COLLECTION_NAME)
            .whereEqualTo("groupId", groupId.value)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()

        return documents.map { it.toObject(FirebaseTest::class.java).toSharedWorkbook(it.id) }
    }

    override suspend fun findWorkbookById(documentId: DocumentId): SharedWorkbook? {
        val document = db.collection(WORKBOOK_COLLECTION_NAME)
            .document(documentId.value)
            .get()
            .await()

        return document.toObject(FirebaseTest::class.java)
            ?.toSharedWorkbook(documentId = document.id)

    }

    override suspend fun createWorkbook(workbook: SharedWorkbook) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWorkbook(userId: UserId, workbookId: DocumentId) {
        db.collection(WORKBOOK_COLLECTION_NAME)
            .document(workbookId.value)
            .delete()
            .await()
        _updateWorkbookListFlow.emit(getWorkbookListByUserId(userId = userId))
    }

    override suspend fun deleteWorkbookFromGroup(groupId: GroupId, workbookId: DocumentId) {
        db.collection(WORKBOOK_COLLECTION_NAME)
            .document(workbookId.value)
            .delete()
            .await()
        _updateGroupWorkbookListFlow.emit(getWorkbookListByGroupId(groupId = groupId))
    }

    override suspend fun shareWorkbook(documentId: DocumentId): Uri =
        dynamicLinksCreator.createShareWorkbookDynamicLinks(documentId = documentId.value).shortLink
            ?: Uri.EMPTY

    override suspend fun getQuestionListByWorkbookId(documentId: DocumentId): List<SharedQuestion> {
        val documents = db.collection(WORKBOOK_COLLECTION_NAME)
            .document(documentId.value)
            .collection(QUESTION_COLLECTION_NAME)
            .get()
            .await()

        return documents.map {
            it.toObject(FirebaseQuestion::class.java).toSharedQuestion(it.id)
        }.sortedBy { it.order }
    }
}
