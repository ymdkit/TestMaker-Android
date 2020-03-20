package jp.gr.java_conf.foobar.testmaker.service.view.edit

import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.itemAddCategory
import jp.gr.java_conf.foobar.testmaker.service.itemCategory

class CategoryController(private val onClickAddCategoryListener: OnClickAddCategoryListener) : EpoxyController() {

    private var listener: OnClickListener = object : OnClickListener {
        override fun onClickCategory(category: Category) {
            selectedCategory = category
        }

        override fun onClickCancelCategory() {
            selectedCategory = null
        }
    }

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
        fun onClickCancelCategory()
    }

    interface OnClickAddCategoryListener {
        fun onClickAddCategory()
    }

    override fun buildModels() {

        selectedCategory?.let {
            itemCategory {
                id(it.id)
                category(it)
                listener(listener)
                selected(true)
            }
        } ?: run {

            itemAddCategory {
                id("add_category")
                onClick { view ->
                    onClickAddCategoryListener.onClickAddCategory()
                }
            }

            categories.forEach {
                itemCategory {
                    id(it.id)
                    category(it)
                    listener(listener)
                    selected(false)
                }
            }
        }
    }
}