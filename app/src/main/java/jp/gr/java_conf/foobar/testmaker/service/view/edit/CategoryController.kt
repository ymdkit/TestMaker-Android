package jp.gr.java_conf.foobar.testmaker.service.view.edit

import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.itemAddCategory
import jp.gr.java_conf.foobar.testmaker.service.itemCategory
import jp.gr.java_conf.foobar.testmaker.service.itemCategoryHeader

class CategoryController(private val onCategoryEventListener: OnCategoryEventListener) : EpoxyController() {

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

    var isOpen: Boolean = false
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnCategoryEventListener {
        fun onClickAddCategory()
        fun onClickDeleteCategory(category: Category)
    }

    override fun buildModels() {

        selectedCategory?.let {
            itemCategory {
                id(it.id)
                category(it)
                selected(true)
                onClickCancel { _ ->
                    selectedCategory = null
                }
            }
        } ?: run {

            itemCategoryHeader {
                id("header_category")
                isOpen(isOpen)
                onClick { _ ->
                    isOpen = !isOpen
                }
            }
            if (!isOpen) return@run

            itemAddCategory {
                id("add_category")
                onClick { _ ->
                    onCategoryEventListener.onClickAddCategory()
                }
            }

            categories.forEach {
                itemCategory {
                    id(it.id)
                    category(it)
                    selected(false)
                    onClick { _ ->
                        selectedCategory = it
                    }
                    onClickDelete { _ ->
                        onCategoryEventListener.onClickDeleteCategory(it)
                    }
                }
            }

        }
    }
}