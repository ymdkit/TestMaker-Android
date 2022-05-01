package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import com.example.infra.local.entity.RealmTest
import com.google.firebase.Timestamp
import com.squareup.moshi.Json
import jp.gr.java_conf.foobar.testmaker.service.R
import java.text.SimpleDateFormat
import java.util.*

// フィールド名に is を含めるとフィールド名が正しく保存されない場合があります（2020/12/20）
data class FirebaseTest(var name: String = "",
                        var color: Int = 0,
                        @Json(name = "user_id") var userId: String = "",
                        @Json(name = "user_name") var userName: String = "",
                        @Json(name = "comment") var overview: String = "",
                        var locale: String = "",
                        var size: Int = 0,
                        var created_at: Timestamp = Timestamp(Date()),
                        @Json(name = "document_id") var documentId: String = "",
                        @Json(name = "download_count") var downloadCount: Int = 0,
                        @Json(name = "answer_count") var answerCount: Int = 0,
                        var public: Boolean = true,
                        var groupId: String = ""
) {


    var questions: List<FirebaseQuestion> = listOf()

    fun toTest(context: Context): RealmTest {
        val test = RealmTest()
        test.limit = 100
        test.title = name
        test.color = context.resources.getIntArray(R.array.color_list)[Math.min(Math.abs(color), 7)]
        test.documentId = documentId
        return test
    }

    fun getDate(): String {

        val date = Date(created_at.seconds * 1000)
        val df = SimpleDateFormat("yyyy-MM-dd")


        return df.format(date)
    }

}