package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.airbnb.epoxy.EpoxyTouchHelper
import com.example.ui.core.AdViewModel
import com.example.ui.question.QuestionListViewModel
import com.example.usecase.model.QuestionUseCaseModel
import com.google.android.gms.ads.AdRequest
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.ItemQuestionBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentQuestionListBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by keita on 2017/02/12.
 */

@AndroidEntryPoint
class QuestionListFragment : Fragment() {

    private val args: QuestionListFragmentArgs by navArgs()
    private val questionListViewModel: QuestionListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var logger: TestMakerLogger

    private val controller: EditController by lazy {
        EditController(requireContext()).apply {
            setOnClickListener(object : EditController.OnClickListener {
                override fun onClickQuestion(question: QuestionUseCaseModel) {

                    if (actionMode != null) {
                        selectedQuestions = if (selectedQuestions.any { question.id == it.id }) {
                            selectedQuestions.filterNot { it.id == question.id }
                        } else {
                            selectedQuestions + listOf(question)
                        }
                    } else {

                        ListDialogFragment.newInstance(
                            question.problem,
                            listOf(
                                DialogMenuItem(
                                    title = getString(R.string.edit),
                                    iconRes = R.drawable.ic_edit_white,
                                    action = { editQuestion(question) }),
                                DialogMenuItem(
                                    title = getString(R.string.copy_question),
                                    iconRes = R.drawable.ic_baseline_file_copy_24,
                                    action = { copyQuestion(question) }),
                                DialogMenuItem(
                                    title = getString(R.string.delete),
                                    iconRes = R.drawable.ic_delete_white,
                                    action = { deleteQuestion(question) })
                            )
                        ).show(childFragmentManager, "TAG")

                    }
                }
            })
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater: MenuInflater = mode?.menuInflater ?: return false
            inflater.inflate(R.menu.menu_edit_selected, menu)
            // ContextMenu の背景色に合わせてアイコンの色を変更
            menu?.forEach {
                val drawable = it.icon
                DrawableCompat.setTint(
                    drawable,
                    ContextCompat.getColor(requireContext(), R.color.colorText)
                )
                it.icon = drawable
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_move -> {
                    val workbookList =
                        questionListViewModel.uiState.value.workbookList.getOrNull() ?: return true

                    if (controller.selectedQuestions.isEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_selected_questions))
                        return true
                    }

                    ListDialogFragment.newInstance(
                        getString(R.string.msg_move_questions),
                        workbookList.filterNot { it.id == args.workbookId }.map {
                            DialogMenuItem(
                                title = it.name,
                                iconRes = R.drawable.ic_baseline_description_24,
                                action = {

                                    questionListViewModel.moveQuestionsToOtherWorkbook(
                                        destWorkbookId = it.id,
                                        questionList = controller.selectedQuestions
                                    )

                                    requireContext().showToast(
                                        getString(
                                            R.string.msg_succes_move_questions,
                                            it.name
                                        )
                                    )
                                    logger.logEvent("move_questions")
                                    mode?.finish()
                                }
                            )
                        }
                    ).show(childFragmentManager, "TAG")
                    return true
                }
                R.id.action_copy -> {
                    val workbookList =
                        questionListViewModel.uiState.value.workbookList.getOrNull() ?: return true

                    if (controller.selectedQuestions.isEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_selected_questions))
                        return true
                    }

                    ListDialogFragment.newInstance(
                        getString(R.string.msg_copy_questions),
                        workbookList.filterNot { it.id == args.workbookId }.map {
                            DialogMenuItem(
                                title = it.name,
                                iconRes = R.drawable.ic_baseline_description_24,
                                action = {
                                    questionListViewModel.copyQuestionsToOtherWorkbook(
                                        destWorkbookId = it.id,
                                        questionList = controller.selectedQuestions
                                    )
                                    requireContext().showToast(
                                        getString(
                                            R.string.msg_succes_copy_questions,
                                            it.name
                                        )
                                    )
                                    logger.logEvent("copy_questions")
                                    mode?.finish()
                                }
                            )
                        }
                    ).show(childFragmentManager, "TAG")
                    return true
                }
                R.id.action_delete -> {

                    if (controller.selectedQuestions.isEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_selected_questions))
                        return true
                    }

                    ConfirmDangerDialogFragment.newInstance(
                        getString(
                            R.string.msg_delete_selected_questions,
                        ),
                        getString(R.string.button_delete_confirm)
                    ) {

                        questionListViewModel.deleteQuestions(controller.selectedQuestions)

                        requireContext().showToast(getString(R.string.msg_succes_delete_questions))
                        logger.logEvent("delete_questions")
                        mode?.finish()

                    }.show(childFragmentManager, "TAG")
                    return true
                }
                else -> {
                    return false
                }
            }
        }


        override fun onDestroyActionMode(mode: ActionMode?) {
            controller.selectedQuestions = emptyList()
            actionMode = null
        }

    }

    private lateinit var binding: FragmentQuestionListBinding

    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return DataBindingUtil.inflate<FragmentQuestionListBinding>(
            inflater,
            R.layout.fragment_question_list,
            container,
            false
        ).apply {
            binding = this

            recyclerView.adapter = controller.adapter

            fab.setOnClickListener {
                findNavController().navigate(
                    QuestionListFragmentDirections
                        .actionQuestionListToCreateQuestion(args.workbookId)
                )
            }

            toolbar.setupWithNavController(
                findNavController(),
                AppBarConfiguration(findNavController().graph)
            )

            lifecycleScope.launch {
                adViewModel.isRemovedAd.onEach {
                    if (it) {
                        adView.visibility = View.GONE
                    } else {
                        adView.loadAd(AdRequest.Builder().build())
                    }
                }
            }

            initViews()

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        questionListViewModel.setup(workbookId = args.workbookId)
        questionListViewModel.load()

        lifecycleScope.launchWhenCreated {
            questionListViewModel.uiState.onEach {
                val workbook = it.workbook.getOrNull() ?: return@onEach
                controller.questions = workbook.questionList

                val exportedWorkbook = it.exportedWorkbook.getOrNull() ?: return@onEach
                shareExportedWorkbook(exportedWorkbook = exportedWorkbook)

            }.launchIn(this)
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_setting -> {
                    findNavController().navigate(
                        QuestionListFragmentDirections.actionQuestionListToEditWorkbook(
                            workbookId = args.workbookId
                        )
                    )
                    true
                }
                R.id.action_export -> {
                    questionListViewModel.exportWorkbook()
                    true
                }
                android.R.id.home -> {
                    findNavController().popBackStack()
                    true
                }
                R.id.action_reset_achievement -> {
                    questionListViewModel
                        .resetWorkbookAchievement()

                    requireContext().showToast(getString(R.string.msg_reset_achievement))
                    true
                }
                R.id.action_select -> {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    } else {
                        controller.selectedQuestions = emptyList()
                    }
                    true
                }
                else -> {
                    super.onOptionsItemSelected(it)
                }
            }
        }

        val searchView = binding.toolbar.menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                controller.searchWord = s
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                controller.searchWord = s
                return false
            }
        })

        searchView.setOnCloseListener {
            controller.searchWord = ""
            false
        }
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    private fun initViews() {
        EpoxyTouchHelper
            .initDragging(controller)
            .withRecyclerView(binding.recyclerView)
            .forVerticalList()
            .withTarget(ItemQuestionBindingModel_::class.java)
            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<ItemQuestionBindingModel_>() {
                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: ItemQuestionBindingModel_,
                    itemView: View?
                ) {
                    val from = controller.adapter.getModelAtPosition(fromPosition)
                    val to = controller.adapter.getModelAtPosition(toPosition)

                    if (from is ItemQuestionBindingModel_ && to is ItemQuestionBindingModel_) {
                        questionListViewModel.swapQuestions(from.questionId(), to.questionId())
                    }
                }
            })
    }

    private fun shareExportedWorkbook(exportedWorkbook: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, exportedWorkbook)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    fun editQuestion(question: QuestionUseCaseModel) {
        findNavController().navigate(
            QuestionListFragmentDirections.actionQuestionListToEditQuestion(
                workbookId = args.workbookId,
                questionId = question.id
            )
        )
    }

    fun copyQuestion(question: QuestionUseCaseModel) {
        questionListViewModel.copyQuestionInSameWorkbook(
            question = question
        )
        requireContext().showToast(getString(R.string.msg_succes_copy_questions_in_same_workbook))
    }

    fun deleteQuestion(question: QuestionUseCaseModel) {
        ConfirmDangerDialogFragment.newInstance(
            getString(R.string.message_delete, question.problem),
            getString(R.string.button_delete_confirm)
        ) {
            questionListViewModel.deleteQuestions(listOf(question))
        }.show(childFragmentManager, "TAG")
    }
}


