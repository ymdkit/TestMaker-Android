package com.example.infra.remote.entity

import com.example.core.TestMakerColor
import com.example.domain.model.DocumentId
import com.example.domain.model.GroupId
import com.example.domain.model.SharedWorkbook
import com.example.domain.model.UserId
import com.google.firebase.Timestamp
import java.util.*

// フィールド名に is を含めるとフィールド名が正しく保存されない場合があります（2020/12/20）
data class FirebaseTest(
    var name: String = "",
    var color: Int = 0,
    var userId: String = "",
    var userName: String = "",
    var overview: String = "",
    var locale: String = "",
    var size: Int = 0,
    var created_at: Timestamp = Timestamp(Date()),
    var documentId: String = "",
    var downloadCount: Int = 0,
    var answerCount: Int = 0,
    var public: Boolean = true,
    var groupId: String = ""
) {

    companion object {
        fun fromSharedWorkbook(workbook: SharedWorkbook) =
            FirebaseTest().apply {
                documentId = workbook.id.value
                name = workbook.name
                color = workbook.color.ordinal
                userId = workbook.userId.value
                userName = workbook.userName
                overview = workbook.comment
                locale = Locale.getDefault().language
                size = workbook.questionListCount
                created_at = Timestamp.now()
                public = workbook.isPublic
                groupId = workbook.groupId?.value ?: ""
            }
    }

    fun toSharedWorkbook(documentId: String): SharedWorkbook =
        SharedWorkbook(
            id = DocumentId(documentId),
            name = name,
            color = TestMakerColor.values()[color.coerceIn(0, TestMakerColor.values().lastIndex)],
            userId = UserId(value = userId),
            userName = userName,
            comment = overview,
            questionListCount = size,
            downloadCount = downloadCount,
            isPublic = public,
            groupId = GroupId(groupId),
        )
}