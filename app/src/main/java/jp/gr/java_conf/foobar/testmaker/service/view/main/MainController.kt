package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import jp.gr.java_conf.foobar.testmaker.service.*

class MainController(
    private val context: Context
) : EpoxyController() {

    private var listener: OnClickListener? = null

    var folderList: List<FolderUseCaseModel> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    var workbookList: List<WorkbookUseCaseModel> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickTest(workbook: WorkbookUseCaseModel)
        fun onClickFolderMenu(folder: FolderUseCaseModel)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun isStickyHeader(position: Int): Boolean {
        if (position >= adapter.itemCount) return false
        return adapter.getModelAtPosition(position)::class == ItemSectionHeaderBindingModel_::class
    }

    override fun buildModels() {

        if (workbookList.isEmpty() && folderList.isEmpty()) {
            itemEmpty {
                id("empty")
                message(context.getString(R.string.empty_test))
            }
            return
        }

        if (folderList.isNotEmpty()) {
            itemSectionHeader {
                id("Folder")
                title(context.getString(R.string.folder))
            }
        }

        folderList.forEach {
            cardCategory {
                id("category-${it.id}")
                color(it.color)
                name(it.name)
                size(
                    context.getString(
                        R.string.number_exams,
                        it.workbookCount
                    )
                )
                onClick { _, _, _, _ ->
                    // todo フォルダ詳細画面への遷移
                }
                onClickMenu { _, _, _, _ ->
                    listener?.onClickFolderMenu(it)
                }
            }
        }

        if (workbookList.isNotEmpty()) {
            itemSectionHeader {
                id("Test")
                title(context.getString(R.string.test))
            }
        }

        workbookList.forEach {
            itemTest {
                id("workbook-${it.id}")
                name(it.name)
                color(it.color)
                size(
                    context.getString(
                        R.string.number_existing_questions,
                        it.correctCount,
                        it.questionCount
                    )
                )
                onClick { _ -> listener?.onClickTest(it) }
            }
        }
    }
}