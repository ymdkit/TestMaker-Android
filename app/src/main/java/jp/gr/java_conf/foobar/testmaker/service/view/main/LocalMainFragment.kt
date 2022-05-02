package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import com.example.ui.home.HomeViewModel
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.CardCategoryBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.ItemTestBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.LocalMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LocalMainFragment : Fragment() {

    private val mainController by lazy { MainController(requireContext()) }

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private var binding: LocalMainFragmentBinding? = null

    private val homeViewModel: HomeViewModel by viewModels()

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
                            action = { playWorkbook(workbook) }),
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
                    CardCategoryBindingModel_::class.java,
                    ItemTestBindingModel_::class.java
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

                        if (from is ItemTestBindingModel_ && to is ItemTestBindingModel_) {
                            homeViewModel.swapWorkbook(from.id(), to.id())
                        } else if (from is CardCategoryBindingModel_ && to is CardCategoryBindingModel_) {
                            homeViewModel.swapFolder(from.id(), to.id())
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
        }

    }

    private fun playWorkbook(workbook: WorkbookUseCaseModel) {
        if (workbook.questionCount == 0) {
            requireContext().showToast(getString(R.string.message_null_questions))
            return
        }
        initDialogPlayStart(workbook)
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

    private fun initDialogPlayStart(workbook: WorkbookUseCaseModel) {

        if (!sharedPreferenceManager.isShowPlaySettingDialog) {
            startAnswer(workbook)
        } else {
// todo 一時的にコメントアウト
//            childFragmentManager.setFragmentResultListener(
//                REQUEST_PLAY_CONFIG,
//                viewLifecycleOwner
//            ) { requestKey, bundle ->
//                if (requestKey != REQUEST_PLAY_CONFIG) return@setFragmentResultListener
//
//                startAnswer(workbook)
//            }
//
//            PlayConfigDialogFragment.newInstance(workbook, REQUEST_PLAY_CONFIG)
//                .show(childFragmentManager, "TAG")
        }
    }

    private fun startAnswer(workbook: WorkbookUseCaseModel) {

        if (workbook.inCorrectCount == 0 && sharedPreferenceManager.refine) {
            requireContext().showToast(getString(R.string.message_null_wrongs))
        } else {
            // todo 移行
//            val result = workbook.copy(
//                limit = limit,
//                startPosition = start,
//                history = Calendar.getInstance().timeInMillis
//            )
//
//            testViewModel.update(result)

            findNavController().navigate(
                HomeFragmentDirections.actionHomeToAnswerWorkbook(
                    workbookId = workbook.id,
                    isRetry = false
                )
            )
        }
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

    companion object {
        const val REQUEST_PLAY_CONFIG = "request_play_config"
    }
}