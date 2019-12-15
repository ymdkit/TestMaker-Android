package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.cardOnlineMyPage
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import kotlin.math.abs

class FirebaseMyPageController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    var tests: List<DocumentSnapshot> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickDownloadTest(document: DocumentSnapshot)
        fun onClickDetailTest(document: DocumentSnapshot)
        fun onClickDeleteTest(document: DocumentSnapshot)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        tests.forEach {

            val data = it.toObject(FirebaseTest::class.java) ?: return

            cardOnlineMyPage {
                size(context.getString(R.string.num_questions,data.size))
                title(data.name)
                id(it.id)
                document(it)
                colorId(context.resources.getIntArray(R.array.color_list)[abs(data.color).coerceAtMost(7)])
                listener(listener)
            }

        }


    }

}