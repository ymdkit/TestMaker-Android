package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.cardTestOnline
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import kotlin.math.abs

class FirebaseTestController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    var tests: List<FirebaseTest> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickTest(test: FirebaseTest)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        tests.forEach {
            cardTestOnline {
                id(it.documentId)
                test(it.apply {
                    this.color = context.resources.getIntArray(R.array.color_list)[abs(it.color).coerceAtMost(7)]
                })
                listener(listener)
            }
        }
    }
}