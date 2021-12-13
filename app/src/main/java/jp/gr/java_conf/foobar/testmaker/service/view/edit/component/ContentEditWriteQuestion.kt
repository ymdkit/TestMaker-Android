package jp.gr.java_conf.foobar.testmaker.service.view.edit.component

import android.graphics.Bitmap
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import jp.gr.java_conf.foobar.testmaker.service.view.edit.ImageStore

@Composable
fun ContentEditWriteQuestion(
    onCreate: (QuestionModel) -> Unit,
    questionId: Long,
    order: Int,
    imageUrl: String,
    fragmentManager: FragmentManager
) {

    var problem by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    var showingValidationError by remember { mutableStateOf(false) }

    val validate =
        !(problem.isEmpty() || answer.isEmpty())

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        Column(
            modifier = Modifier
                .weight(weight = 1f, fill = true)
                .scrollable(ScrollState(0), orientation = Orientation.Vertical)
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
                value = problem,
                maxLines = 3,
                label = {
                    Text(text = stringResource(R.string.hint_question))
                },
                onValueChange = {
                    problem = it
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = answer,
                label = {
                    Text(text = stringResource(R.string.hint_answer))
                },
                onValueChange = {
                    answer = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.header_optional),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.caption
            )
            ContentEditImageQuestion(
                fragmentManager = fragmentManager,
                onBitmapChange = {
                    bitmap = it
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = explanation,
                maxLines = 3,
                label = {
                    Text(text = stringResource(R.string.hint_explanation))
                },
                onValueChange = {
                    explanation = it
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
                    problem = problem,
                    answer = answer,
                    answers = listOf(),
                    wrongChoices = listOf(),
                    format = QuestionFormat.WRITE,
                    explanation = explanation,
                    imageUrl = newImageUrl,
                    isCheckOrder = false,
                    isAnswering = false,
                    isAutoGenerateWrongChoices = false,
                    answerStatus = AnswerStatus.UNANSWERED,
                    order = order,
                )

                onCreate(question)

                problem = ""
                answer = ""
                explanation = ""
                bitmap = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = stringResource(id = R.string.button_create_wuestion))
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