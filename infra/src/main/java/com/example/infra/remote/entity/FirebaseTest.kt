package com.example.infra.remote.entity

import com.example.core.TestMakerColor
import com.example.domain.model.DocumentId
import com.example.domain.model.SharedWorkbook
import com.example.domain.model.UserId
import com.example.infra.local.entity.RealmTest
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
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

    var questions: List<FirebaseQuestion> = listOf()

    @Deprecated("remove this method as soon as possible")
    fun toTest(): RealmTest {
        val test = RealmTest()
        test.limit = 100
        test.title = name
        test.themeColor = TestMakerColor.BLUE.name
        test.documentId = documentId
        return test
    }

    fun getDate(): String {

        val date = Date(created_at.seconds * 1000)
        val df = SimpleDateFormat("yyyy-MM-dd")


        return df.format(date)
    }

    fun toSharedWorkbook(documentId: String): SharedWorkbook =
        SharedWorkbook(
            id = DocumentId(documentId),
            name = name,
            userId = UserId(value = userId),
            userName = userName,
            questionListCount = size,
            downloadCount = downloadCount,
            isPublic = public,
            groupId = groupId,
        )

}