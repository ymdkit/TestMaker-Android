package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.cardTestAccount
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.itemEmpty
import kotlin.math.abs

class AccountMainController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    var tests: List<DocumentSnapshot> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickTest(document: DocumentSnapshot)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        if (tests.isEmpty()) {
            itemEmpty {
                id("empty")
                message(context.getString(R.string.empty_uploaded_test))
            }
            return
        }

        tests.forEach {

            val data = it.toObject(FirebaseTest::class.java) ?: return

            cardTestAccount {
                size(context.getString(R.string.num_questions, data.size))
                title(data.name)
                id(it.id)
                document(it)
                publicity(if (data.public) context.getString(R.string.label_public) else context.getString(R.string.label_private))
                colorId(context.resources.getIntArray(R.array.color_list)[abs(data.color).coerceAtMost(7)])
                listener(listener)
            }

        }


    }

}