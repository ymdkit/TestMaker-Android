package com.example.infra.repository

import com.example.domain.repository.UserLogRepository
import com.example.infra.local.db.WorkbookDataSource
import com.example.infra.local.entity.RealmTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class UserLogRepositoryImpl @Inject constructor(
    private val workbookDataSource: WorkbookDataSource,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : UserLogRepository {

    override suspend fun uploadUserLog() {
        val userId = auth.uid ?: UUID.randomUUID().toString()
        val userRef = db
            .collection("user_logs")
            .document(userId)

        var workbookList: List<RealmTest>
        withContext(Dispatchers.Main) {
            workbookList = workbookDataSource.getWorkbookList()
        }

        workbookList.forEach { workbook ->
            val workbookRef = userRef
                .collection("workbooks")
                .document(workbook.id.toString())

            workbookRef.set(
                mapOf(
                    "userId" to userId,
                    "workbookId" to workbook.id.toString(),
                    "title" to workbook.title,
                )
            )

            val batchOperationLimit = 500
            workbook.getQuestions().chunked(batchOperationLimit).forEach {
                db.runBatch { batch ->
                    it.forEach {
                        val questionRef =
                            workbookRef.collection("questions").document(it.id.toString())
                        batch.set(
                            questionRef,
                            mapOf(
                                "userId" to userId,
                                "workbookId" to workbook.id,
                                "questionId" to it.id,
                                "problem" to it.problem,
                                "answer" to it.answer,
                            )
                        )
                    }
                }
            }
        }
    }
}