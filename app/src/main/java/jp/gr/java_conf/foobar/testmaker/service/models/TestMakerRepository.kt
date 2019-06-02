package jp.gr.java_conf.foobar.testmaker.service.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException


class TestMakerRepository(private val local: LocalDataSource,
                          private val remote: RemoteDataSource) {

    var tests: MutableLiveData<List<Test>>? = null
        private set

    var questions: MutableLiveData<ArrayList<Quest>>? = null
        private set

    fun getTests(): LiveData<List<Test>> {
        if (tests == null) {
            tests = MutableLiveData()
            fetchTests()
        }
        return tests as LiveData<List<Test>>
    }

    private fun fetchTests() {
        GlobalScope.launch(Dispatchers.Main) {
            tests?.postValue(local.getTests())
        }
    }

    fun getQuestions(testId: Long): LiveData<ArrayList<Quest>> {
        if (questions == null) {
            questions = MutableLiveData()
            fetchQuestions(testId)
        }
        return questions as LiveData<ArrayList<Quest>>
    }

    fun fetchQuestions(testId: Long) {
        GlobalScope.launch(Dispatchers.Main) {
                questions?.postValue(local.getQuestions(testId))
        }
    }

    fun clearQuestions() {
        questions = null
    }

    fun deleteQuestion(question: Quest) {
        local.deleteQuestion(question)
    }

    fun loadImage(imagePath: String, setImage: (Bitmap) -> Unit) {
        local.loadImage(imagePath,setImage)
    }

    fun saveImage(fileName: String,bitmap: Bitmap) {
        local.saveImage(fileName,bitmap)
    }

}