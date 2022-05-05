package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.*
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import kotlin.math.abs

class GroupDetailController(private val context: Context) : EpoxyController() {

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

    override fun isStickyHeader(position: Int): Boolean {
        return adapter.getModelAtPosition(position)::class == SectionHeaderBindingModel_::class
    }

    override fun buildModels() {

        if (tests.isEmpty()) {
            empty {
                id("empty")
                message(this@GroupDetailController.context.getString(R.string.empty_uploaded_test))
            }
            return
        }

        sectionHeader {
            id("Test")
            title(this@GroupDetailController.context.getString(R.string.test))
        }

        tests.forEach {

            val data = it.toObject(FirebaseTest::class.java) ?: return

            testGroup {
                size(
                    this@GroupDetailController.context.getString(
                        R.string.num_questions,
                        data.size
                    )
                )
                title(data.name)
                id(it.id)
                document(it)
                colorId(
                    this@GroupDetailController.context.resources.getIntArray(R.array.color_list)[abs(
                        data.color
                    ).coerceAtMost(7)]
                )
                listener(this@GroupDetailController.listener)
            }

        }


    }

}