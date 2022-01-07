package jp.gr.java_conf.foobar.testmaker.service.view.edit.component

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.AnswerStatus
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.extensions.replaced
import jp.gr.java_conf.foobar.testmaker.service.view.edit.ImageStore
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.NumberPicker
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.OutlinedSwitch

@Composable
fun ContentEditSelectCompleteQuestion(
    onCreate: (QuestionModel) -> Unit,
    questionId: Long,
    initialProblem: String,
    initialAnswers: List<String>,
    initialWrongChoices: List<String>,
    initialExplanation: String,
    initialIsCheckAnswerOrder: Boolean,
    initialIsAutoGenerateWrongChoices: Boolean,
    order: Int,
    initialImageUrl: String,
    buttonTitle: String,
    fragmentManager: FragmentManager
) {

    var editingProblem by rememberSaveable { mutableStateOf(initialProblem) }
    var editingExplanation by rememberSaveable { mutableStateOf(initialExplanation) }
    var editingImageUrl by rememberSaveable { mutableStateOf(initialImageUrl) }
    var editingAnswers by rememberSaveable {
        mutableStateOf(List(ANSWERS_SIZE_MAX) {
            if (it < initialAnswers.size) {
                initialAnswers[it]
            } else {
                ""
            }
        })
    }
    var editingWrongChoices by rememberSaveable {
        mutableStateOf(List(WRONG_SIZE_MAX) {
            if (it < initialWrongChoices.size) {
                initialWrongChoices[it]
            } else {
                ""
            }
        })
    }
    var sizeOfAnswers by rememberSaveable {
        mutableStateOf(if (initialAnswers.isNotEmpty()) initialAnswers.size else 2)
    }
    var sizeOfWrongChoices by rememberSaveable {
        mutableStateOf(if (initialWrongChoices.isNotEmpty()) initialWrongChoices.size else 2)
    }
    var isCheckAnswerOrder by rememberSaveable {
        mutableStateOf(initialIsCheckAnswerOrder)
    }
    var isAutoGenerateWrongChoices by rememberSaveable {
        mutableStateOf(initialIsAutoGenerateWrongChoices)
    }

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    var showingValidationError by rememberSaveable { mutableStateOf(false) }

    fun validate(): Boolean{
        if(editingProblem.isEmpty()) return false
        if(editingAnswers.take(sizeOfAnswers).any { it.isEmpty() }) return false
        if(!isAutoGenerateWrongChoices && editingWrongChoices.take(sizeOfWrongChoices).any { it.isEmpty() }) return false
        return true
    }

    val focusRequester = remember { FocusRequester() }
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

                    label = {
                        Text(text = stringResource(R.string.hint_answer))
                    },
                    value = editingAnswers[index],
                    onValueChange = {
                        editingAnswers = editingAnswers.replaced(index, it)
                    }
                )
            }
            repeat(sizeOfWrongChoices.coerceIn(0, editingWrongChoices.size)) { index ->

                if (isAutoGenerateWrongChoices) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        enabled = false,
                        value = stringResource(R.string.hint_auto),
                        onValueChange = {}
                    )
                } else {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        value = editingWrongChoices[index],

                        label = {
                            Text(text = stringResource(R.string.hint_other))
                        },
                        onValueChange = {
                            editingWrongChoices = editingWrongChoices.replaced(index, it)
                        }
                    )
                }
            }

            NumberPicker(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                min = 0,
                max = ANSWERS_SIZE_MAX,
                label = stringResource(id = R.string.picker_answers_size),
                value = sizeOfAnswers,
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
                value = sizeOfWrongChoices,
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
                value = bitmap,
                onValueChange = {
                    bitmap = it
                    if (bitmap == null) {
                        editingImageUrl = ""
                    }
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = editingExplanation,

                label = {
                    Text(text = stringResource(R.string.hint_explanation))
                },
                onValueChange = {
                    editingExplanation = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.header_other_settings),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.caption
            )

            OutlinedSwitch(
                modifier = Modifier.padding(bottom = 8.dp),
                label = stringResource(id = R.string.switch_is_check_order),
                checked = isCheckAnswerOrder,
                onCheckedChange = {
                    isCheckAnswerOrder = it
                })
            OutlinedSwitch(
                modifier = Modifier.padding(bottom = 8.dp),
                label = stringResource(id = R.string.switch_is_auto_generate_others),
                checked = isAutoGenerateWrongChoices,
                onCheckedChange = {
                    isAutoGenerateWrongChoices = it
                })
            Spacer(modifier = Modifier.height((LocalConfiguration.current.screenHeightDp / 3).dp))
        }
        Button(
            onClick = {

                if (!validate()) {
                    showingValidationError = true
                    return@Button
                }

                val newImageUrl = bitmap?.let {
                    ImageStore().saveImage(it, context = context)
                } ?: editingImageUrl

                val question = QuestionModel(
                    id = questionId,
                    problem = editingProblem,
                    answer = "",
                    answers = editingAnswers.take(sizeOfAnswers),
                    wrongChoices = editingWrongChoices.take(sizeOfWrongChoices),
                    format = QuestionFormat.SELECT_COMPLETE,
                    explanation = editingExplanation,
                    imageUrl = newImageUrl,
                    isCheckOrder = isCheckAnswerOrder,
                    isAnswering = false,
                    isAutoGenerateWrongChoices = isAutoGenerateWrongChoices,
                    answerStatus = AnswerStatus.UNANSWERED,
                    order = order,
                )

                onCreate(question)

                editingProblem = ""
                editingExplanation = ""
                editingAnswers = List(ANSWERS_SIZE_MAX) { "" }
                editingWrongChoices = List(WRONG_SIZE_MAX) { "" }
                bitmap = null
                focusRequester.requestFocus()
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