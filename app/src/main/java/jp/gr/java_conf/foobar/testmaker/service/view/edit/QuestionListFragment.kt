package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.airbnb.epoxy.EpoxyTouchHelper
import com.example.ui.core.*
import com.example.ui.question.OperateQuestion
import com.example.ui.question.QuestionListItem
import com.example.ui.question.QuestionListViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.QuestionBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentQuestionListBinding
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.online.SearchTextField
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
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
                                    title = getString(R.string.copy_question),
                                    iconRes = R.drawable.ic_baseline_file_copy_24,
                                    action = { copyQuestion(question) }),
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

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {

                    val uiState by questionListViewModel.uiState.collectAsState()
                    val drawerState =
                        rememberBottomDrawerState(BottomDrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    BottomDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !drawerState.isClosed,
                        drawerContent = {
                            val question = uiState.selectedQuestion
                            if (question != null) {
                                OperateQuestion(
                                    question = question,
                                    onEdit = {
                                        findNavController().navigate(
                                            QuestionListFragmentDirections.actionQuestionListToEditQuestion(
                                                workbookId = args.workbookId,
                                                questionId = question.id
                                            )
                                        )
                                    },
                                    onMove = { /*TODO*/ },
                                    onCopy = {
                                        scope.launch {
                                            drawerState.close()
                                            copyQuestion(question)
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            drawerState.close()
                                            questionListViewModel.deleteQuestions(listOf(question))
                                        }
                                    },
                                )
                            } else {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }) {
                        Column {
                            Scaffold(
                                modifier = Modifier.weight(1f),
                                topBar = {
                                    TopAppBar(
                                        navigationIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .clickable {
                                                        findNavController().popBackStack()
                                                    }
                                            )
                                        },
                                        title = {
                                            if (uiState.isSearching) {
                                                SearchTextField(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    value = uiState.query,
                                                    onValueChange = questionListViewModel::onQueryChanged,
                                                    onSearch = questionListViewModel::load
                                                )
                                            } else {
                                                Text(
                                                    text = stringResource(id = R.string.pgae_list_question),
                                                )
                                            }
                                        },
                                        backgroundColor = Color.Transparent,
                                        elevation = 0.dp,
                                        actions = {
                                            IconButton(
                                                onClick = {
                                                    questionListViewModel.onSearchButtonClicked(!uiState.isSearching)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.isSearching) Icons.Filled.Close else Icons.Filled.Search,
                                                    contentDescription = "search"
                                                )
                                            }
                                        }
                                    )
                                },
                                content = {
                                    when (val state = uiState.questionList) {
                                        is Resource.Success -> {
                                            // todo 0件表示
                                            LazyColumn(
                                                modifier = Modifier.fillMaxHeight()
                                            ) {
                                                itemsIndexed(state.value) { index, it ->
                                                    QuestionListItem(
                                                        index = index + 1,
                                                        question = it,
                                                        onClick = {
                                                            scope.launch {
                                                                questionListViewModel.onQuestionClicked(
                                                                    it
                                                                )
                                                                drawerState.open()
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            // do nothing
                                        }
                                    }

                                },
                                floatingActionButton = {
                                    FloatingActionButton(onClick = {
                                        findNavController().navigate(
                                            QuestionListFragmentDirections
                                                .actionQuestionListToCreateQuestion(args.workbookId)
                                        )
                                    }) {
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = "create question"
                                        )
                                    }
                                }
                            )
                            AdView(viewModel = adViewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adViewModel.setup()
        questionListViewModel.setup(workbookId = args.workbookId)
        questionListViewModel.load()

//        lifecycleScope.launchWhenCreated {
//            questionListViewModel.uiState.onEach {
//
//                val exportedWorkbook = it.exportedWorkbook.getOrNull() ?: return@onEach
//                shareExportedWorkbook(exportedWorkbook = exportedWorkbook)
//
//            }.launchIn(this)
//        }

//        binding.toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.action_setting -> {
//                    findNavController().navigate(
//                        QuestionListFragmentDirections.actionQuestionListToEditWorkbook(
//                            workbookId = args.workbookId
//                        )
//                    )
//                    true
//                }
//                R.id.action_export -> {
//                    questionListViewModel.exportWorkbook()
//                    true
//                }
//                R.id.action_reset_achievement -> {
//                    questionListViewModel
//                        .resetWorkbookAchievement()
//
//                    requireContext().showToast(getString(R.string.msg_reset_achievement))
//                    true
//                }
//                R.id.action_select -> {
//                    if (actionMode == null) {
//                        actionMode = requireActivity().startActionMode(actionModeCallback)
//                    } else {
//                        controller.selectedQuestions = emptyList()
//                    }
//                    true
//                }
//                else -> {
//                    super.onOptionsItemSelected(it)
//                }
//            }
//        }
//
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
            .withTarget(QuestionBindingModel_::class.java)
            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<QuestionBindingModel_>() {
                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: QuestionBindingModel_,
                    itemView: View?
                ) {
                    val from = controller.adapter.getModelAtPosition(fromPosition)
                    val to = controller.adapter.getModelAtPosition(toPosition)

                    if (from is QuestionBindingModel_ && to is QuestionBindingModel_) {
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


    private fun copyQuestion(question: QuestionUseCaseModel) {
        questionListViewModel.copyQuestionInSameWorkbook(
            question = question
        )
        requireContext().showToast(getString(R.string.msg_succes_copy_questions_in_same_workbook))
    }
}


