package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class EditSelectCompleteQuestionViewModel : EditQuestionViewModel() {

    override var selectedQuestion = Question()
        set(value) {
            field = value
            inputForm(field)
        }

    val answer = MutableLiveData("")
    val answers = List(SIZE_ANSWER_MAX) { MutableLiveData("") }
    val others = List(SIZE_OTHER_MAX) { MutableLiveData("") }
    val isCheckedAuto = MutableLiveData(false)
    val sizeOfOthers = MutableLiveData(2)
    val sizeOfAnswers = MutableLiveData(2)

    private val sizeOfTotal: Int
        get() {
            return (sizeOfAnswers.value ?: SIZE_ANSWER_MAX) + (sizeOfOthers.value ?: SIZE_OTHER_MAX)
        }


    val isValidated = MediatorLiveData<Boolean>().also { result ->
        result.addSource(question) { result.value = isValid }
        result.addSource(answer) { result.value = isValid }
        others.forEach {
            result.addSource(it) { result.value = isValid }
        }
        result.addSource(sizeOfOthers) { result.value = isValid }
        answers.forEach {
            result.addSource(it) { result.value = isValid }
        }
        result.addSource(sizeOfAnswers) { result.value = isValid }
    }

    private val isValid: Boolean
        get() = !question.value.isNullOrEmpty() &&
                !others.take(sizeOfOthers.value ?: SIZE_OTHER_MAX).any {
                    it.value.isNullOrEmpty()
                } &&
                !answers.take(sizeOfAnswers.value ?: SIZE_ANSWER_MAX).any {
                    it.value.isNullOrEmpty()
                }

    override fun createQuestion() = selectedQuestion.copy(
            question = question.value ?: "",
            answer = answers.map { it.value ?: "" }.take(sizeOfAnswers.value
                    ?: 0).joinToString(separator = " "),
            answers = answers.map { it.value ?: "" }.take(sizeOfAnswers.value ?: 0),
            others = others.map { it.value ?: "" }.take(sizeOfOthers.value ?: 0),
            explanation = explanation.value ?: "",
            isAutoGenerateOthers = isCheckedAuto.value ?: false,
            type = Constants.SELECT_COMPLETE,
            imagePath = imagePath.value ?: ""
    )

    override fun resetForm() {
        if (isResetForm.value == true) {
            super.resetForm()
            answer.value = ""
            others.forEach {
                it.value = ""
            }
            answers.forEach {
                it.value = ""
            }
        }
    }

    override fun inputForm(question: Question) {
        super.inputForm(question)
        answer.value = question.answer
        sizeOfOthers.value = question.others.size.coerceAtLeast(SIZE_OTHER_MIN)
        sizeOfAnswers.value = question.answers.size.coerceAtLeast(SIZE_ANSWER_MIN)
        isCheckedAuto.value = question.isAutoGenerateOthers
        question.others.forEachIndexed { index, s ->
            if (index >= SIZE_OTHER_MAX) return@forEachIndexed
            others[index].value = s
        }
        question.answers.forEachIndexed { index, s ->
            if (index >= SIZE_ANSWER_MAX) return@forEachIndexed
            answers[index].value = s
        }
        if (sizeOfTotal == 0) {
            sizeOfAnswers.value = 1
        }
    }

    fun mutateSizeOfOthers(num: Int) {

        val result = sizeOfTotal + num
        if (result < SIZE_TOTAL_MIN) return
        if (result > SIZE_TOTAL_MAX) {
            sizeOfAnswers.value = (sizeOfAnswers.value ?: SIZE_ANSWER_MAX) - 1
        }

        sizeOfOthers.value?.let {
            if (it + num in SIZE_OTHER_MIN..SIZE_OTHER_MAX) {
                sizeOfOthers.value = it + num
            }
        }
    }

    fun mutateSizeOfAnswers(num: Int) {
        val result = sizeOfTotal + num
        if (result < SIZE_TOTAL_MIN) return
        if (result > SIZE_TOTAL_MAX) {
            sizeOfOthers.value = (sizeOfOthers.value ?: SIZE_OTHER_MAX) - 1
        }

        sizeOfAnswers.value?.let {
            if (it + num in SIZE_ANSWER_MIN..SIZE_ANSWER_MAX) {
                sizeOfAnswers.value = it + num
            }
        }
    }

    companion object {
        const val SIZE_OTHER_MIN = 0
        const val SIZE_OTHER_MAX = 6
        const val SIZE_ANSWER_MIN = 0
        const val SIZE_ANSWER_MAX = 6
        const val SIZE_TOTAL_MIN = 1
        const val SIZE_TOTAL_MAX = 6
    }
}