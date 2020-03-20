package jp.gr.java_conf.foobar.testmaker.service.view.edit

import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.itemCategory

class CategoryController : EpoxyController() {

    private var listener: OnClickListener? = null

    var categories: List<Category> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    var selectedCategory: Category? = null
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickCategory(category: Category)
        fun onLongClickCategory(category: Category)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        selectedCategory?.let {
            itemCategory {
                id(it.id)
                category(it)
                listener(listener)
            }
        } ?: run {
            categories.forEach {
                itemCategory {
                    id(it.id)
                    category(it)
                    listener(listener)
                }
            }
        }
    }
}