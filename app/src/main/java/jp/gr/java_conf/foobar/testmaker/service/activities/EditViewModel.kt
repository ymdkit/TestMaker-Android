package jp.gr.java_conf.foobar.testmaker.service.activities

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class EditViewModel(private val repository: TestMakerRepository): ViewModel() {

    val formatQuestion: MutableLiveData<Int> = MutableLiveData()
    val stateEditing: MutableLiveData<Int> = MutableLiveData()
    val spinnerAnswersPosition: MutableLiveData<Int> = MutableLiveData()
    val spinnerSelectsPosition: MutableLiveData<Int> = MutableLiveData()
    val isEditingExplanation: MutableLiveData<Boolean> = MutableLiveData()
    val isAuto: MutableLiveData<Boolean> = MutableLiveData()
    val isCheckOrder: MutableLiveData<Boolean> = MutableLiveData()

    init {
        formatQuestion.value = Constants.WRITE
        stateEditing.value = Constants.NOT_EDITING
        isEditingExplanation.value = false
        isAuto.value = false
        isCheckOrder.value = false
    }

    fun editQuestion(){
        stateEditing.value = Constants.EDIT_QUESTION
    }

    fun deleteQuestion(question: Quest){
        repository.deleteQuestion(question)
    }

    fun getQuestions(testId: Long): LiveData<ArrayList<Quest>> {
        return repository.getQuestions(testId)
    }

    fun fetchQuestions(testId: Long){
        repository.fetchQuestions(testId)
    }

    fun clearQuestions() {
        repository.clearQuestions()
    }

    fun loadImage(imagePath: String, setImage: (Bitmap) -> Unit) {
        repository.loadImage(imagePath,setImage)
    }

    fun saveImage(fileName: String,bitmap: Bitmap) {
        repository.saveImage(fileName,bitmap)
    }
}