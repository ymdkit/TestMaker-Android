package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.TestMakerTopAppBar
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.usecase.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R

@AndroidEntryPoint
class HistoryWorkbookFragment : Fragment() {

    private val args: HistoryWorkbookFragmentArgs by navArgs()

    private val historyWorkbookViewModel: HistoryWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {

                val uiState by historyWorkbookViewModel.uiState.collectAsState()

                TestMakerAndroidTheme {
                    Scaffold(topBar = {
                        TestMakerTopAppBar(title = stringResource(id = R.string.history_test_fragment_label))
                    }) {
                        Column {
                            Column(modifier = Modifier.weight(1f))
                            {
                                when (val state = uiState.answerHistoryList) {
                                    is Resource.Success -> {
                                        LazyColumn {
                                            items(state.value) {
                                                ListItem(
                                                    icon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.Description,
                                                            contentDescription = "history"
                                                        )
                                                    },
                                                    text = {
                                                        Text(
                                                            text = stringResource(
                                                                R.string.item_title_history,
                                                                it.userName,
                                                                it.numSolved,
                                                                it.numCorrect
                                                            )
                                                        )
                                                    },
                                                    secondaryText = {
                                                        Text(text = it.createdAt)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        // do nothing
                                    }
                                }
                            }
                            AdView(viewModel = adViewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyWorkbookViewModel.setup(workbookId = args.documentId)
        historyWorkbookViewModel.load()
    }
}