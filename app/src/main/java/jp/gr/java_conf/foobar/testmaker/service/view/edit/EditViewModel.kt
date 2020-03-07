package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.valueNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

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
    var testId: Long = -1L
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

    fun getTest(): Test = repository.getTest(testId)

    fun editQuestion() {
        stateEditing.value = Constants.EDIT_QUESTION
    }

    fun deleteQuestion(question: Quest) {
        repository.deleteQuestion(question)
    }

    fun getQuestions(): LiveData<ArrayList<Quest>> {
        return repository.getQuestions(testId)
    }

    fun fetchQuestions() {
        repository.fetchQuestions(testId)
    }

    fun clearQuestions() {
        repository.clearQuestions()
    }

    suspend fun loadImage() = repository.loadImage(imagePath)

    suspend fun saveImage(bitmap: Bitmap) = repository.saveImage(imagePath, bitmap)

    fun addQuestion(onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        if (question.valueNonNull().isEmpty()) onFailure(context.getString(R.string.message_shortage))

        val quest = Quest()

        quest.type = formatQuestion.valueNonNull()
        quest.problem = question.valueNonNull()
        quest.imagePath = imagePath
        quest.explanation = explanation.valueNonNull()
        quest.order = order

        val form = editingView

        when (formatQuestion.valueNonNull()) {

            Constants.WRITE -> {

                if (answer.valueNonNull().isEmpty()) {

                    onFailure(context.getString(R.string.message_shortage))

                    return
                }
                quest.answer = answer.valueNonNull()

            }
            Constants.SELECT -> {

                if (form is EditSelectView) {
                    if (!form.isFilled()) {

                        onFailure(context.getString(R.string.message_shortage))

                        return
                    }

                    quest.answer = form.getAnswer()
                    quest.setSelections(form.getOthers())
                    quest.auto = repository.isAuto()
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

                    quest.setAnswers(form.getAnswers())
                    form.getAnswers().forEach { quest.answer += "$it " }
                    quest.isCheckOrder = repository.isCheckOrder()
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

                    quest.setAnswers(form.getAnswers())
                    form.getAnswers().forEach { quest.answer += "$it " }
                    quest.setSelections(form.getOthers())
                    quest.auto = repository.isAuto()
                    quest.isCheckOrder = false //todo 後に実装
                } else {
                    return
                }
            }
        }

        repository.addQuestion(testId, quest, questionId)
        fetchQuestions()
        onSuccess()
    }

    fun getTest(testId: Long): Test = repository.getTest(testId)
    fun getCategories() = repository.getCategories()
    fun addCategory(category: Category) = repository.addCategory(category)
    fun deleteCategory(category: Category) = repository.deleteCategory(category)
    fun resetAchievement() = repository.resetAchievement(testId)
    fun sortManual(from: Int, to: Int, testId: Long) = repository.sortManual(from, to, testId)
    fun migrateOrder() = repository.migrateOrder(testId)
    fun updateTest(title: String, color: Int, category: String) = repository.updateTest(getTest(testId), title, color, category)
}