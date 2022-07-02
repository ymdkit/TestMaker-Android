package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.utils.Resource
import com.example.ui.core.*
import com.example.ui.logger.LogEvent

import com.example.ui.question.OperateQuestion
import com.example.ui.question.QuestionListDrawerState
import com.example.ui.question.QuestionListItem
import com.example.ui.question.QuestionListViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.usecase.model.QuestionUseCaseModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.utils.hideKeyboard
import jp.gr.java_conf.foobar.testmaker.service.view.online.SearchTextField
import kotlinx.coroutines.Job
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
    lateinit var analytics: FirebaseAnalytics

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
                                            analytics.logEvent(
                                                LogEvent.QUESTIONS_BUTTON_EDIT_QUESTION.eventName
                                            ) {}
                                            scope.launch {
                                                drawerState.close()
                                                findNavController().navigate(
                                                    QuestionListFragmentDirections.actionQuestionListToEditQuestion(
                                                        workbookId = args.workbookId,
                                                        questionId = state.question.id
                                                    )
                                                )
                                            }
                                        },
                                        onCopy = {
                                            analytics.logEvent(
                                                LogEvent.QUESTIONS_BUTTON_COPY_QUESTION.eventName
                                            ) {}
                                            scope.launch {
                                                drawerState.close()
                                                copyQuestion(state.question)
                                            }
                                        },
                                        onDelete = {
                                            analytics.logEvent(
                                                LogEvent.QUESTIONS_BUTTON_DELETE_QUESTION.eventName
                                            ) {}
                                            scope.launch {
                                                drawerState.close()
                                                questionListViewModel.deleteQuestions(listOf(state.question))
                                                requireContext().showToast(getString(R.string.msg_success_delete_question))
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
                                                        requireActivity().hideKeyboard(windowToken)
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
                                                    onSearch = {
                                                        analytics.logEvent(
                                                            LogEvent.QUESTIONS_BUTTON_SEARCH_QUESTION.eventName
                                                        ) {}
                                                        questionListViewModel.load()
                                                    }
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
                                                    analytics.logEvent(
                                                        LogEvent.QUESTIONS_BUTTON_MOVE_QUESTIONS.eventName
                                                    ) {}
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
                                                    analytics.logEvent(
                                                        LogEvent.QUESTIONS_BUTTON_COPY_QUESTIONS.eventName
                                                    ) {}
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
                                                        analytics.logEvent(
                                                            LogEvent.QUESTIONS_BUTTON_DELETE_QUESTIONS.eventName
                                                        ) {}
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
                                                    analytics.logEvent(
                                                        LogEvent.QUESTIONS_BUTTON_SELECT.eventName
                                                    ) {}
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
                                            if (state.value.isNotEmpty()) {
                                                var overscrollJob by remember {
                                                    mutableStateOf<Job?>(
                                                        null
                                                    )
                                                }
                                                val dragDropListState =
                                                    rememberDragDropListState(onMove = { from, to ->
                                                        // note この部分で state にアクセスしても、初期状態のままなので2回目以降の入れ替わりが正しく動作しない
                                                        questionListViewModel.swapQuestions(
                                                            from,
                                                            to
                                                        )
                                                    })
                                                LazyColumn(
                                                    state = dragDropListState.lazyListState,
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .pointerInput(Unit) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDrag = { change, offset ->
                                                                    change.consume()
                                                                    dragDropListState.onDrag(offset)

                                                                    if (overscrollJob?.isActive == true)
                                                                        return@detectDragGesturesAfterLongPress

                                                                    dragDropListState
                                                                        .checkForOverScroll()
                                                                        .takeIf { it != 0f }
                                                                        ?.let {
                                                                            overscrollJob =
                                                                                scope.launch {
                                                                                    dragDropListState.lazyListState.scrollBy(
                                                                                        it
                                                                                    )
                                                                                }
                                                                        }
                                                                        ?: run { overscrollJob?.cancel() }
                                                                },
                                                                onDragStart = { offset ->
                                                                    dragDropListState.onDragStart(
                                                                        offset
                                                                    )
                                                                },
                                                                onDragEnd = { dragDropListState.onDragInterrupted() },
                                                                onDragCancel = { dragDropListState.onDragInterrupted() }
                                                            )
                                                        }
                                                ) {
                                                    itemsIndexed(state.value) { index, it ->
                                                        QuestionListItem(
                                                            modifier = Modifier.composed {
                                                                val offsetOrNull =
                                                                    dragDropListState.elementDisplacement.takeIf {
                                                                        index == dragDropListState.currentIndexOfDraggedItem
                                                                    }
                                                                Modifier
                                                                    .graphicsLayer {
                                                                        translationY =
                                                                            offsetOrNull ?: 0f
                                                                    }
                                                            },
                                                            index = index + 1,
                                                            isSelected = it.second,
                                                            question = it.first,
                                                            onClick = {
                                                                if (uiState.isSelectMode) {
                                                                    questionListViewModel.onQuestionSelected(
                                                                        it
                                                                    )
                                                                } else {
                                                                    analytics.logEvent(
                                                                        LogEvent.QUESTIONS_ITEM_OPERATE_QUESTION.eventName
                                                                    ) {}
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
                                            } else {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = stringResource(
                                                            id = R.string.empty_question
                                                        )
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
                                        analytics.logEvent(
                                            LogEvent.QUESTIONS_BUTTON_CREATE_QUESTION.eventName
                                        ) {}
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

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, LogEvent.QUESTIONS_SCREEN_OPEN.eventName)
        }
    }
}


