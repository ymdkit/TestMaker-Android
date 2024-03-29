package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.utils.Resource
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.TestMakerTopAppBar
import com.example.ui.history.HistoryWorkbookViewModel
import com.example.ui.theme.TestMakerAndroidTheme
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
                        TestMakerTopAppBar(
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
                            title = stringResource(id = R.string.history_test_fragment_label)
                        )
                    }) { padding ->
                        Column(
                            modifier = Modifier.padding(padding)
                        ) {
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