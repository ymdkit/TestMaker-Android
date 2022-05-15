package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ui.core.*
import com.example.ui.sharedworkbook.SharedWorkbookListViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PublishedWorkbookListFragment : Fragment() {

    private val sharedWorkbookListViewModel: SharedWorkbookListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()


    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalGraphicsApi::class
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by sharedWorkbookListViewModel.uiState.collectAsState()
                val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
                val scope = rememberCoroutineScope()

                TestMakerAndroidTheme {
                    BottomDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !drawerState.isClosed,
                        drawerContent = {
                            val workbook = uiState.selectedSharedWorkbook

                            if (workbook != null) {
                                ListItem(
                                    text = {
                                        Text(
                                            text = workbook.name,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                                ListItem(
                                    text = {
                                        Text(text = stringResource(id = R.string.workbook_post_user))
                                    },
                                    secondaryText = {
                                        Text(
                                            text = workbook.userName
                                        )
                                    }
                                )
                                if (workbook.comment.isNotEmpty()) {
                                    ListItem(
                                        text = {
                                            Text(text = stringResource(id = R.string.overview))
                                        },
                                        secondaryText = {
                                            Text(
                                                text = workbook.comment
                                            )
                                        }
                                    )
                                }
                                ClickableListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Download,
                                            contentDescription = "download"
                                        )
                                    },
                                    text = stringResource(id = R.string.download)
                                ) {
                                    sharedWorkbookListViewModel.onDownloadWorkbookClicked(workbook = workbook)
                                }
                                ClickableListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "share"
                                        )
                                    },
                                    text = stringResource(id = R.string.share)
                                ) {
                                    scope.launch {
                                        sharedWorkbookListViewModel.onShareWorkbookClicked(workbook = workbook)
                                        drawerState.close()
                                    }
                                }
                                ClickableListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Report,
                                            contentDescription = "report"
                                        )
                                    },
                                    text = stringResource(id = R.string.report)
                                ) {
                                    scope.launch {
                                        drawerState.close()
                                        reportWorkbook(documentId = workbook.id)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }
                    ) {
                        Column {
                            Scaffold(
                                modifier = Modifier.weight(1f),
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            if (uiState.isSearching) {
                                                SearchTextField(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    value = uiState.query,
                                                    onValueChange = sharedWorkbookListViewModel::onQueryChanged,
                                                    onSearch = sharedWorkbookListViewModel::load
                                                )
                                            } else {
                                                Text(
                                                    text = getString(R.string.label_public_tests),
                                                )
                                            }
                                        },
                                        backgroundColor = Color.Transparent,
                                        elevation = 0.dp,
                                        actions = {
                                            IconButton(
                                                onClick =
                                                sharedWorkbookListViewModel::onSearchButtonClicked
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.isSearching) Icons.Filled.Close else Icons.Filled.Search,
                                                    contentDescription = "search"
                                                )
                                            }
                                        },
                                    )
                                },
                                content = {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .weight(weight = 1f, fill = true)
                                                .fillMaxWidth()
                                        ) {
                                            SwipeRefresh(
                                                state = rememberSwipeRefreshState(uiState.isRefreshing),
                                                onRefresh = sharedWorkbookListViewModel::load
                                            ) {
                                                ResourceContent(
                                                    resource = uiState.workbookList,
                                                    onRetry = { sharedWorkbookListViewModel::load }) {
                                                    LazyColumn(
                                                        modifier = Modifier.fillMaxHeight()
                                                    ) {
                                                        items(it) {
                                                            ClickableListItem(
                                                                icon = {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Description,
                                                                        contentDescription = "workbook",
                                                                        modifier = Modifier
                                                                            .size(40.dp)
                                                                            .padding(8.dp),
                                                                        tint = ColorMapper(
                                                                            LocalContext.current
                                                                        ).colorToGraphicColor(it.color)
                                                                    )
                                                                },
                                                                text = it.name,
                                                                secondaryText = stringResource(
                                                                    id = R.string.text_workbook_size,
                                                                    it.questionListCount
                                                                ),
                                                                onClick = {
                                                                    scope.launch {
                                                                        sharedWorkbookListViewModel.onWorkbookClicked(
                                                                            it
                                                                        )
                                                                        drawerState.open()
                                                                    }
                                                                })
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                floatingActionButton = {
                                    FloatingActionButton(onClick = {
                                        findNavController().navigate(
                                            PublishedWorkbookListFragmentDirections.actionSearchToUploadWorkbook()
                                        )
                                    }) {
                                        Icon(
                                            Icons.Filled.CloudUpload,
                                            contentDescription = "upload workbook"
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
        super.onViewCreated(view, savedInstanceState)
        sharedWorkbookListViewModel.setup()
        sharedWorkbookListViewModel.load()

        lifecycleScope.launchWhenCreated {
            sharedWorkbookListViewModel.shareWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    startActivity(
                        actionSendIntent(
                            text = getString(R.string.msg_share_test, it.first, it.second)
                        )
                    )
                }
                .launchIn(this)

            sharedWorkbookListViewModel.downloadWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.msg_success_download_test))
                    val hostActivity = requireActivity() as? MainActivity
                    hostActivity?.navigateHomePage()
                }
                .launchIn(this)
        }
    }

    private fun actionSendIntent(text: String) =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                text
            )
            type = "text/plain"
        }, null)

    private fun reportWorkbook(documentId: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("testmaker.contact@gmail.com"))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.report_subject, documentId)
        )
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_body))
        startActivity(Intent.createChooser(emailIntent, null))
    }
}

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {

    val focusRequester by remember { mutableStateOf(FocusRequester()) }
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onSearch()
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
        modifier = modifier.focusRequester(focusRequester),
        textStyle = TextStyle(color = MaterialTheme.colors.onBackground)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
