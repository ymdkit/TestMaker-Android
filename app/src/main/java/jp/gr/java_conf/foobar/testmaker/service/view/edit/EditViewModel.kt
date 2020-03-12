package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.extensions.valueNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class EditViewModel(private val repository: TestMakerRepository, val context: Context) : ViewModel() {

    val formatQuestion: MutableLiveData<Int> = MutableLiveData()
    val stateEditing: MutableLiveData<Int> = MutableLiveData()
    val spinnerAnswersPosition: MutableLiveData<Int> = MutableLiveData()
    val spinnerSelectsPosition: MutableLiveData<Int> = MutableLiveData()
    val isEditingExplanation: MutableLiveData<Boolean> = MutableLiveData()
    val isAuto: MutableLiveData<Boolean> = MutableLiveData()
    val isCheckOrder: MutableLiveData<Boolean> = MutableLiveData()

    val question: MutableLiveData<String> = MutableLiveData()
    val answer: MutableLiveData<String> = MutableLiveData()
    val explanation: MutableLiveData<String> = MutableLiveData()

    var imagePath: String = ""
    var order: Int = -1
    var questionId: Long = -1
    var editingView: View? = null


    init {
        spinnerAnswersPosition.value = 0
        spinnerSelectsPosition.value = 0
        formatQuestion.value = Constants.WRITE
        stateEditing.value = Constants.NOT_EDITING
        isEditingExplanation.value = false
        isAuto.value = false
        isCheckOrder.value = false
        question.value = ""
        explanation.value = ""
    }

    fun editQuestion() {
        stateEditing.value = Constants.EDIT_QUESTION
    }

    suspend fun loadImage() = repository.loadImage(imagePath)

    suspend fun saveImage(bitmap: Bitmap) = repository.saveImage(imagePath, bitmap)

    fun createQuestion(onSuccess: (Question) -> Unit, onFailure: (String) -> Unit) {
        if (question.valueNonNull().isEmpty()) onFailure(context.getString(R.string.message_shortage))

        var question = Question(
                id = questionId,
                question = question.valueNonNull(),
                type = formatQuestion.valueNonNull(),
                imagePath = imagePath,
                explanation = explanation.valueNonNull(),
                order = order
        )

        val form = editingView

        when (formatQuestion.valueNonNull()) {

            Constants.WRITE -> {

                if (answer.valueNonNull().isEmpty()) {

                    onFailure(context.getString(R.string.message_shortage))

                    return
                }
                question = question.copy(answer = answer.valueNonNull())

            }
            Constants.SELECT -> {

                if (form is EditSelectView) {
                    if (!form.isFilled()) {

                        onFailure(context.getString(R.string.message_shortage))

                        return
                    }

                    question = question.copy(
                            answer = form.getAnswer(),
                            others = form.getOthers(),
                            isAutoGenerateOthers = repository.isAuto())

                } else {
                    return
                }
            }

            Constants.COMPLETE -> {

                if (form is EditCompleteView) {
                    if (!form.isFilled()) {
                        onFailure(context.getString(R.string.message_shortage))
                        return
                    }

                    if (form.isDuplicate() && !repository.isCheckOrder()) {
                        onFailure(context.getString(R.string.message_answer_duplicate))
                        return
                    }

                    question = question.copy(
                            answer = form.getAnswers().joinToString(separator = " "),
                            answers = form.getAnswers(),
                            isCheckOrder = repository.isCheckOrder())

                } else {
                    return
                }
            }
            Constants.SELECT_COMPLETE -> {

                if (form is EditSelectCompleteView) {
                    if (!form.isFilled()) {
                        onFailure(context.getString(R.string.message_shortage))
                        return
                    }

                    if (context.resources.getStringArray(R.array.spinner_selects_complete)[spinnerSelectsPosition.valueNonNull()].toInt()
                            < context.resources.getStringArray(R.array.spinner_answers_select_complete)[spinnerAnswersPosition.valueNonNull()].toInt()) {
                        onFailure(context.getString(R.string.message_answers_num))
                        return
                    }

                    question = question.copy(
                            answer = form.getAnswers().joinToString(separator = " "),
                            answers = form.getAnswers(),
                            others = form.getOthers(),
                            isAutoGenerateOthers = repository.isAuto(),
                            isCheckOrder = repository.isCheckOrder())
                } else {
                    return
                }
            }
        }

        onSuccess(question)

    }
}