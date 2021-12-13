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

@Composable
fun ContentEditSelectCompleteQuestion(
    onCreate: (QuestionModel) -> Unit,
    questionId: Long,
    initialProblem: String,
    initialAnswers: List<String>,
    initialWrongChoices: List<String>,
    initialExplanation: String,
    order: Int,
    initialImageUrl: String,
    buttonTitle: String,
    fragmentManager: FragmentManager
) {

    var editingProblem by remember { mutableStateOf(initialProblem) }
    var editingExplanation by remember { mutableStateOf(initialExplanation) }
    var editingImageUrl by remember { mutableStateOf(initialImageUrl) }
    var editingAnswers by remember {
        mutableStateOf(List(ANSWERS_SIZE_MAX) {
            if (it < initialAnswers.size) {
                initialAnswers[it]
            } else {
                ""
            }
        })
    }
    var editingWrongChoices by remember {
        mutableStateOf(List(WRONG_SIZE_MAX) {
            if (it < initialWrongChoices.size) {
                initialWrongChoices[it]
            } else {
                ""
            }
        })
    }
    var sizeOfAnswers by remember {
        mutableStateOf(if (initialAnswers.isNotEmpty()) initialAnswers.size else 2)
    }
    var sizeOfWrongChoices by remember {
        mutableStateOf(if (initialWrongChoices.isNotEmpty()) initialWrongChoices.size else 2)
    }

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    var showingValidationError by remember { mutableStateOf(false) }

    val validate =
        !(editingProblem.isEmpty() ||
                editingAnswers.take(sizeOfAnswers).any { it.isEmpty() } ||
                editingWrongChoices.take(sizeOfWrongChoices).any { it.isEmpty() }
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


            repeat(sizeOfAnswers.coerceIn(0, editingAnswers.size)) { index ->

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    value = editingAnswers[index],
                    label = {
                        Text(text = stringResource(R.string.hint_answer))
                    },
                    onValueChange = {
                        editingAnswers = editingAnswers.replaced(index, it)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    })
                )
            }
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
                min = 0,
                max = ANSWERS_SIZE_MAX,
                label = stringResource(id = R.string.picker_answers_size),
                initialValue = sizeOfAnswers,
                onValueChange = {
                    sizeOfAnswers = it
                    editingAnswers = List(ANSWERS_SIZE_MAX) { index ->
                        if (index < sizeOfAnswers) {
                            editingAnswers[index]
                        } else {
                            ""
                        }
                    }
                })

            NumberPicker(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                min = 0,
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
                imageUrl = editingImageUrl,
                fragmentManager = fragmentManager,
                onBitmapChange = {
                    bitmap = it
                    if(bitmap == null){
                        editingImageUrl = ""
                    }
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
                } ?: editingExplanation

                val question = QuestionModel(
                    id = questionId,
                    problem = editingProblem,
                    answer = "",
                    answers = editingAnswers.take(sizeOfAnswers),
                    wrongChoices = editingWrongChoices.take(sizeOfWrongChoices),
                    format = QuestionFormat.SELECT_COMPLETE,
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
                editingExplanation = ""
                editingAnswers = List(ANSWERS_SIZE_MAX) { "" }
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