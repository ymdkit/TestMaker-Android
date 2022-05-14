package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.core.*
import com.example.ui.question.OperateQuestion
import com.example.ui.question.QuestionListDrawerState
import com.example.ui.question.QuestionListItem
import com.example.ui.question.QuestionListViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.online.SearchTextField
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
                            when (val state = uiState.drawerState) {
                                is QuestionListDrawerState.None -> {
                                    Spacer(modifier = Modifier.height(1.dp))
                                }
                                is QuestionListDrawerState.OperateQuestion -> {
                                    OperateQuestion(
                                        question = state.question,
                                        onEdit = {
                                            findNavController().navigate(
                                                QuestionListFragmentDirections.actionQuestionListToEditQuestion(
                                                    workbookId = args.workbookId,
                                                    questionId = state.question.id
                                                )
                                            )
                                        },
                                        onCopy = {
                                            scope.launch {
                                                drawerState.close()
                                                copyQuestion(state.question)
                                            }
                                        },
                                        onDelete = {
                                            scope.launch {
                                                drawerState.close()
                                                questionListViewModel.deleteQuestions(listOf(state.question))
                                            }
                                        },
                                    )
                                }
                                is QuestionListDrawerState.SelectMoveDestinationWorkbook -> {
                                    LazyColumn {
                                        item {
                                            ListItem {
                                                Text(
                                                    text = stringResource(id = R.string.title_select_move_dest_work_book),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        items(state.workbookList) {
                                            ListItem(
                                                modifier = Modifier.clickable {
                                                    scope.launch {
                                                        questionListViewModel.moveQuestionsToOtherWorkbook(
                                                            it.id
                                                        )
                                                        questionListViewModel.onSelectModeChanged(
                                                            false
                                                        )
                                                        drawerState.close()
                                                        requireContext().showToast(
                                                            getString(
                                                                R.string.msg_succes_move_questions,
                                                                it.name
                                                            )
                                                        )
                                                    }
                                                },
                                                text = { Text(it.name) }
                                            )
                                        }
                                    }
                                }
                                is QuestionListDrawerState.SelectCopyDestinationWorkbook -> {
                                    LazyColumn {
                                        item {
                                            ListItem {
                                                Text(
                                                    text = stringResource(id = R.string.title_select_copy_dest_work_book),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        items(state.workbookList) {
                                            ListItem(
                                                modifier = Modifier.clickable {
                                                    scope.launch {
                                                        questionListViewModel.copyQuestionsToOtherWorkbook(
                                                            it.id
                                                        )
                                                        questionListViewModel.onSelectModeChanged(
                                                            false
                                                        )
                                                        drawerState.close()
                                                        requireContext().showToast(
                                                            getString(
                                                                R.string.msg_succes_copy_questions,
                                                                it.name
                                                            )
                                                        )
                                                    }

                                                },
                                                text = { Text(it.name) }
                                            )
                                        }
                                    }
                                }
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
                                            if (uiState.isSelectMode) {
                                                IconButton(onClick = {
                                                    scope.launch {
                                                        questionListViewModel.onMoveQuestionListButtonClicked()
                                                        drawerState.open()
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.DriveFileMove,
                                                        contentDescription = "move"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    scope.launch {
                                                        questionListViewModel.onCopyQuestionListButtonClicked()
                                                        drawerState.open()
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.FileCopy,
                                                        contentDescription = "copy"
                                                    )
                                                }
                                                ConfirmActionIconButton(
                                                    onConfirmed = {
                                                        questionListViewModel.deleteSelectedQuestionList()
                                                        questionListViewModel.onSelectModeChanged(
                                                            false
                                                        )
                                                        requireContext().showToast(getString(R.string.msg_succes_delete_questions))
                                                    },
                                                    confirmMessage = stringResource(id = R.string.msg_delete_selected_questions),
                                                    confirmButtonText = stringResource(id = R.string.button_delete_confirm)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "delete"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    questionListViewModel.onSelectModeChanged(false)
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "close"
                                                    )
                                                }
                                            } else {
                                                IconButton(
                                                    onClick = {
                                                        questionListViewModel.onSearchButtonClicked(
                                                            !uiState.isSearching
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (uiState.isSearching) Icons.Filled.Close else Icons.Filled.Search,
                                                        contentDescription = "search"
                                                    )
                                                }
                                                TextButton(onClick = {
                                                    questionListViewModel.onSelectModeChanged(true)
                                                }) {
                                                    Text(
                                                        text = stringResource(id = R.string.menu_select),
                                                        color = MaterialTheme.colors.onBackground
                                                    )
                                                }
                                                IconButton(onClick = questionListViewModel::onMenuToggleButtonClicked) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "more"
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = uiState.showingMenu,
                                                    onDismissRequest = questionListViewModel::onMenuToggleButtonClicked
                                                ) {
                                                    DropdownMenuItem(
                                                        onClick = {
                                                            findNavController().navigate(
                                                                QuestionListFragmentDirections.actionQuestionListToEditWorkbook(
                                                                    workbookId = args.workbookId
                                                                )
                                                            )
                                                            questionListViewModel.onMenuToggleButtonClicked()
                                                        }
                                                    ) {
                                                        Text(stringResource(id = R.string.title_edit_workbook))
                                                    }
                                                    DropdownMenuItem(
                                                        onClick = {
                                                            questionListViewModel.onMenuToggleButtonClicked()
                                                            questionListViewModel.exportWorkbook()
                                                        }
                                                    ) {
                                                        Text(stringResource(id = R.string.menu_export))
                                                    }
                                                    ConfirmActionDropDownMenu(
                                                        label = stringResource(id = R.string.reset_achievement),
                                                        confirmMessage = stringResource(
                                                            id = R.string.msg_reset_achievement,
                                                        ),
                                                        confirmButtonText = stringResource(id = R.string.reset),
                                                        onConfirmed = questionListViewModel::resetWorkbookAchievement
                                                    )
                                                }
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
                                                        isSelected = it.second,
                                                        question = it.first,
                                                        onClick = {
                                                            if (uiState.isSelectMode) {
                                                                questionListViewModel.onQuestionSelected(
                                                                    it
                                                                )
                                                            } else {
                                                                scope.launch {
                                                                    questionListViewModel.onQuestionClicked(
                                                                        it
                                                                    )
                                                                    drawerState.open()
                                                                }
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

        lifecycleScope.launchWhenCreated {
            questionListViewModel.exportWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    shareExportedWorkbook(exportedWorkbook = it)
                }
                .launchIn(this)
        }
    }

//    private fun initViews() {
//        EpoxyTouchHelper
//            .initDragging(controller)
//            .withRecyclerView(binding.recyclerView)
//            .forVerticalList()
//            .withTarget(QuestionBindingModel_::class.java)
//            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<QuestionBindingModel_>() {
//                override fun onModelMoved(
//                    fromPosition: Int,
//                    toPosition: Int,
//                    modelBeingMoved: QuestionBindingModel_,
//                    itemView: View?
//                ) {
//                    val from = controller.adapter.getModelAtPosition(fromPosition)
//                    val to = controller.adapter.getModelAtPosition(toPosition)
//
//                    if (from is QuestionBindingModel_ && to is QuestionBindingModel_) {
//                        questionListViewModel.swapQuestions(from.questionId(), to.questionId())
//                    }
//                }
//            })
//    }

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


