package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import com.google.firebase.Timestamp
import jp.gr.java_conf.foobar.testmaker.service.R
import java.text.SimpleDateFormat
import java.util.*

data class FirebaseTest(var name: String = "",
                        var color: Int = 0,
                        var userId: String = "",
                        var userName: String = "",
                        var overview: String = "",
                        var locale: String = "",
                        var size: Int = 0,
                        var created_at: Timestamp = Timestamp(Date())
) {

    var questions: List<FirebaseQuestion> = listOf()

    fun toTest(context: Context): Test {
        val test = Test()
        test.limit = 100
        test.title = name
        test.color = context.resources.getIntArray(R.array.color_list)[Math.min(Math.abs(color), 7)]
        return test
    }

    fun getDate(): String {

        val date = Date(created_at.seconds * 1000)
        val df = SimpleDateFormat("yyyy-MM-dd")


        return df.format(date)
    }

}