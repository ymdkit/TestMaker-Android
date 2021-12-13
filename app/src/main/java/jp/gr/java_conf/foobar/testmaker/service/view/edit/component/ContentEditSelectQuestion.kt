package jp.gr.java_conf.foobar.testmaker.service.view.edit.component

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.AnswerStatus
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.extensions.replaced
import jp.gr.java_conf.foobar.testmaker.service.view.edit.ImageStore
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.NumberPicker

const val WRONG_SIZE_MAX = 10

@Composable
fun ContentEditSelectQuestion(
    onCreate: (QuestionModel) -> Unit,
    questionId: Long,
    problem: String,
    answer: String,
    wrongChoices: List<String>,
    explanation: String,
    order: Int,
    imageUrl: String,
    buttonTitle: String,
    fragmentManager: FragmentManager
) {

    var editingProblem by remember { mutableStateOf(problem) }
    var editingAnswer by remember { mutableStateOf(answer) }
    var editingExplanation by remember { mutableStateOf(explanation) }
    var editingWrongChoices by remember {
        mutableStateOf(List(WRONG_SIZE_MAX) {
            if (it < wrongChoices.size) {
                wrongChoices[it]
            } else {
                ""
            }
        })
    }
    var sizeOfWrongChoices by remember {
        mutableStateOf(if (wrongChoices.isNotEmpty()) wrongChoices.size else 3)
    }

    var showingDropDownSizeOfWrongChoices by remember {
        mutableStateOf(false)
    }

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    var showingValidationError by remember { mutableStateOf(false) }

    val validate =
        !(editingProblem.isEmpty()
                || editingAnswer.isEmpty()
                || editingWrongChoices.take(sizeOfWrongChoices).any { it.isEmpty() }
                )

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .weight(weight = 1f, fill = true)
        ) {
            Text(
                text = stringResource(id = R.string.header_required),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.caption
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 8.dp),
                value = editingProblem,
                maxLines = 3,
                label = {
                    Text(text = stringResource(R.string.hint_question))
                },
                onValueChange = {
                    editingProblem = it
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = editingAnswer,
                label = {
                    Text(text = stringResource(R.string.hint_answer))
                },
                onValueChange = {
                    editingAnswer = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )

            repeat(sizeOfWrongChoices.coerceIn(0, editingWrongChoices.size)) { index ->
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    value = editingWrongChoices[index],
                    maxLines = 3,
                    label = {
                        Text(text = stringResource(R.string.hint_other))
                    },
                    onValueChange = {
                        editingWrongChoices = editingWrongChoices.replaced(index, it)
                    }
                )
            }

            NumberPicker(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                min = 1,
                max = WRONG_SIZE_MAX,
                label = stringResource(id = R.string.picker_wrong_size),
                initialValue = sizeOfWrongChoices,
                onValueChange = {
                    sizeOfWrongChoices = it
                    editingWrongChoices = List(WRONG_SIZE_MAX) { index ->
                        if (index < sizeOfWrongChoices) {
                            editingWrongChoices[index]
                        } else {
                            ""
                        }
                    }
                })

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.header_optional),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.caption
            )
            ContentEditImageQuestion(
                imageUrl = imageUrl,
                fragmentManager = fragmentManager,
                onBitmapChange = {
                    bitmap = it
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = editingExplanation,
                maxLines = 3,
                label = {
                    Text(text = stringResource(R.string.hint_explanation))
                },
                onValueChange = {
                    editingExplanation = it
                }
            )
        }
        Button(
            onClick = {
                if (!validate) {
                    showingValidationError = true
                    return@Button
                }

                val newImageUrl = bitmap?.let {
                    ImageStore().saveImage(it, context = context)
                } ?: imageUrl

                val question = QuestionModel(
                    id = questionId,
                    problem = editingProblem,
                    answer = editingAnswer,
                    answers = listOf(),
                    wrongChoices = editingWrongChoices,
                    format = QuestionFormat.WRITE,
                    explanation = editingExplanation,
                    imageUrl = newImageUrl,
                    isCheckOrder = false,
                    isAnswering = false,
                    isAutoGenerateWrongChoices = false,
                    answerStatus = AnswerStatus.UNANSWERED,
                    order = order,
                )

                onCreate(question)

                editingProblem = ""
                editingAnswer = ""
                editingExplanation = ""
                editingWrongChoices = List(WRONG_SIZE_MAX) { "" }
                bitmap = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = buttonTitle)
        }
        if (showingValidationError) {
            AlertDialog(
                onDismissRequest = {
                    showingValidationError = false
                },
                title = {
                    Text(stringResource(id = R.string.title_error_create_quesiton))
                },
                text = {
                    Text(stringResource(id = R.string.msg_error_create_quesiton))
                },
                confirmButton = {
                    TextButton(onClick = {
                        showingValidationError = false
                    }) {
                        Text(stringResource(id = R.string.ok))
                    }
                }
            )
        }
    }
}