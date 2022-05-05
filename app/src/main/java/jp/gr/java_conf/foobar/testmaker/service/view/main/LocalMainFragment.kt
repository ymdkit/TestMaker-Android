package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import com.example.ui.core.DialogMenuItem
import com.example.ui.core.ListDialogFragment
import com.example.ui.core.showToast
import com.example.ui.home.HomeViewModel
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.FolderBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.TestBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.databinding.LocalMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class LocalMainFragment : Fragment() {

    private val mainController by lazy { MainController(requireContext()) }


    private var binding: LocalMainFragmentBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    @Inject
    lateinit var logger: TestMakerLogger

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mainController.setOnClickListener(object : MainController.OnClickListener {

            override fun onClickTest(workbook: WorkbookUseCaseModel) {
                ListDialogFragment.newInstance(
                    workbook.name,
                    listOf(
                        DialogMenuItem(
                            title = getString(R.string.play),
                            iconRes = R.drawable.ic_play_arrow_white_24dp,
                            action = { homeViewModel.onAnswerWorkbookClicked(workbook) }),
                        DialogMenuItem(
                            title = getString(R.string.edit),
                            iconRes = R.drawable.ic_edit_white,
                            action = { editWorkbook(workbook) }),
                        DialogMenuItem(
                            title = getString(R.string.delete),
                            iconRes = R.drawable.ic_delete_white,
                            action = { deleteWorkbook(workbook) }),
                        DialogMenuItem(
                            title = getString(R.string.share),
                            iconRes = R.drawable.ic_share_white,
                            action = { uploadWorkbook(workbook) })
                    )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }

            override fun onClickFolderMenu(folder: FolderUseCaseModel) {

                ListDialogFragment.newInstance(
                    folder.name,
                    listOf(
                        DialogMenuItem(
                            title = getString(R.string.edit_category_name),
                            iconRes = R.drawable.ic_edit_white,
                            action = { editFolder(folder) }),
                        DialogMenuItem(
                            title = getString(R.string.delete),
                            iconRes = R.drawable.ic_delete_white,
                            action = { deleteCategory(folder) })
                    )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }
        })

        return DataBindingUtil.inflate<LocalMainFragmentBinding>(
            inflater,
            R.layout.local_main_fragment,
            container,
            false
        ).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner

            fab.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeToCreateWorkbook())
            }

            recyclerView.layoutManager = StickyHeaderLinearLayoutManager(requireContext())
            recyclerView.adapter = mainController.adapter

            EpoxyTouchHelper
                .initDragging(mainController)
                .withRecyclerView(recyclerView)
                .forVerticalList()
                .withTargets(
                    FolderBindingModel_::class.java,
                    TestBindingModel_::class.java
                )
                .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<EpoxyModel<*>>() {
                    override fun onModelMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        modelBeingMoved: EpoxyModel<*>?,
                        itemView: View?
                    ) {
                        val from = mainController.adapter.getModelAtPosition(fromPosition)
                        val to = mainController.adapter.getModelAtPosition(toPosition)

                        if (from is TestBindingModel_ && to is TestBindingModel_) {
                            homeViewModel.swapWorkbook(from.workbookId(), to.workbookId())
                        } else if (from is FolderBindingModel_ && to is FolderBindingModel_) {
                            homeViewModel.swapFolder(from.folderId(), to.folderId())
                        }
                    }
                })
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.setup()
        homeViewModel.load()

        lifecycleScope.launchWhenCreated {
            homeViewModel.uiState.onEach {
                val uiState = it.getOrNull() ?: return@onEach
                mainController.workbookList = uiState.workBookList
                mainController.folderList = uiState.folderList
            }.launchIn(this)

            homeViewModel.navigateToAnswerSettingEvent
                .receiveAsFlow()
                .onEach {
                    AnswerSettingDialogFragment.newInstance(
                        workbookId = it.workbookId,
                        workbookName = it.workbookName
                    )
                        .show(childFragmentManager, "TAG")
                }
                .launchIn(this)

            homeViewModel.navigateToAnswerWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeToAnswerWorkbook(
                            workbookId = it.workbookId,
                            isRetry = it.isRetry
                        )
                    )
                }
                .launchIn(this)

            homeViewModel.questionListEmptyEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.message_null_questions))
                }
                .launchIn(this)
        }

    }

    private fun editWorkbook(workbook: WorkbookUseCaseModel) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToListQuestion(
                workbook.id
            )
        )
    }

    private fun deleteWorkbook(workbook: WorkbookUseCaseModel) {
        ConfirmDangerDialogFragment.newInstance(
            title = getString(R.string.message_delete_exam, workbook.name),
            buttonText = getString(R.string.button_delete_confirm),
            completion = {
                homeViewModel.deleteWorkbook(workbook)
                requireContext().showToast(getString(R.string.msg_success_delete_test))
            }
        ).show(childFragmentManager, ConfirmDangerDialogFragment::class.java.simpleName)
    }

    private fun uploadWorkbook(workbook: WorkbookUseCaseModel) {
        logger.logEvent("upload_from_share_local")
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToShareWorkbook(
                workbookId = workbook.id
            )
        )
    }

    private fun editFolder(folder: FolderUseCaseModel) {

        EditTextDialogFragment.newInstance(
            title = getString(R.string.title_dialog_edit_category),
            defaultText = folder.name,
            hint = getString(R.string.hint_category_name)
        )
        { newFolderName ->
            homeViewModel.updateFolder(folder, newFolderName)
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun deleteCategory(folder: FolderUseCaseModel) {
        ConfirmDangerDialogFragment.newInstance(
            title = getString(R.string.message_delete_category, folder.name),
            buttonText = getString(R.string.button_delete_confirm),
            completion = {
                homeViewModel.deleteFolder(folder = folder)
                requireContext().showToast(getString(R.string.msg_success_delete_category))
            }
        ).show(childFragmentManager, ConfirmDangerDialogFragment::class.java.simpleName)
    }
}