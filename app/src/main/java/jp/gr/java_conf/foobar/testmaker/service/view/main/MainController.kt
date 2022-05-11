package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.example.ui.core.ColorMapper
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
        return adapter.getModelAtPosition(position)::class == SectionHeaderBindingModel_::class
    }

    override fun buildModels() {
        val colorMapper = ColorMapper(context)

        if (workbookList.isEmpty() && folderList.isEmpty()) {
            empty {
                id("empty")
                message(this@MainController.context.getString(R.string.empty_test))
            }
            return
        }

        if (folderList.isNotEmpty()) {
            sectionHeader {
                id("Folder")
                title(this@MainController.context.getString(R.string.folder))
            }
        }

        folderList.forEach {
            folder {
                id("category-${it.id}")
                folderId(it.id)
                color(colorMapper.colorToLegacyGraphicColor(it.color))
                name(it.name)
                size(
                    this@MainController.context.getString(
                        R.string.number_exams,
                        it.workbookCount
                    )
                )
                onClick { _, _, _, _ ->
                    // todo フォルダ詳細画面への遷移
                }
                onClickMenu { _, _, _, _ ->
                    this@MainController.listener?.onClickFolderMenu(it)
                }
            }
        }

        if (workbookList.isNotEmpty()) {
            sectionHeader {
                id("Test")
                title(this@MainController.context.getString(R.string.test))
            }
        }

        workbookList.forEach {
            test {
                id("workbook-${it.id}")
                workbookId(it.id)
                name(it.name)
                color(colorMapper.colorToLegacyGraphicColor(it.color))
                size(
                    this@MainController.context.getString(
                        R.string.number_existing_questions,
                        it.correctCount,
                        it.questionCount
                    )
                )
                onClick { _ -> this@MainController.listener?.onClickTest(it) }
            }
        }
    }
}