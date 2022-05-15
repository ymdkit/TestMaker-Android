package com.example.infra.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.core.QuestionImage
import com.example.core.TestMakerColor
import com.example.domain.model.*
import com.example.domain.repository.SharedWorkbookRepository
import com.example.infra.remote.DynamicLinksCreator
import com.example.infra.remote.SearchApi
import com.example.infra.remote.SearchClient
import com.example.infra.remote.entity.FirebaseQuestion
import com.example.infra.remote.entity.FirebaseTest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class SharedWorkbookRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    @SearchClient private val searchApi: SearchApi,
    @ApplicationContext private val context: Context,
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

    override suspend fun createWorkbook(
        user: User,
        groupId: GroupId?,
        isPublic: Boolean,
        workbook: Workbook,
        comment: String
    ) {
        val workbookRef = db.collection(WORKBOOK_COLLECTION_NAME).document()
        val workbookDocumentId = workbookRef.id
        val batchOperationLimit = 500

        workbook.questionList.sortedBy { it.order }.chunked(batchOperationLimit - 1)
            .forEachIndexed { index, list ->

                val newWorkbookName = if (index == 0) workbook.name else "${workbook.name}($index)"

                val newWorkbook = FirebaseTest.fromSharedWorkbook(
                    SharedWorkbook(
                        id = DocumentId(workbookDocumentId),
                        name = newWorkbookName,
                        // todo 色設定
                        color = TestMakerColor.BLUE,
                        userId = user.id,
                        userName = user.displayName,
                        comment = comment,
                        questionListCount = workbook.questionList.size,
                        downloadCount = 0,
                        isPublic = isPublic,
                        groupId = groupId,
                    )
                )

                db.runBatch { batch ->
                    batch.set(
                        workbookRef,
                        newWorkbook
                    )
                    list.forEach {
                        val questionRef = db
                            .collection(WORKBOOK_COLLECTION_NAME)
                            .document(workbookDocumentId)
                            .collection(QUESTION_COLLECTION_NAME)
                            .document()


                        val newImageUrl =
                            when (val problemImage = it.problemImageUrl) {
                                is QuestionImage.LocalImage -> {
                                    val imageRef = "${user.id}/${problemImage.getRawString()}"
                                    uploadImage(problemImage.getRawString(), imageRef)
                                    imageRef
                                }
                                else -> {
                                    problemImage.getRawString()
                                }
                            }
                        // todo 画像のアップロード

                        batch.set(
                            questionRef,
                            FirebaseQuestion.fromSharedQuestion(
                                SharedQuestion(
                                    id = DocumentId(questionRef.id),
                                    problem = it.problem,
                                    explanation = it.explanation,
                                    answerList = it.answers,
                                    otherSelectionList = it.otherSelections,
                                    problemImageUrl = newImageUrl,
                                    explanationImageUrl = "",
                                    questionType = it.type,
                                    isCheckAnswerOrder = it.isCheckAnswerOrder,
                                    isAutoGenerateOtherSelections = it.isAutoGenerateOtherSelections,
                                    order = it.order
                                )
                            )
                        )
                    }
                }.await()
            }
        _updateWorkbookListFlow.emit(getWorkbookListByUserId(userId = user.id))
    }

    private fun uploadImage(localPath: String, remotePath: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child(remotePath)

        val baos = ByteArrayOutputStream()
        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        runCatching {
            context.openFileInput(localPath)
        }.onSuccess {
            val bitmap = BitmapFactory.decodeStream(it, null, imageOptions)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()
            storageRef.putBytes(data)
        }
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
