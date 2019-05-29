package jp.gr.java_conf.foobar.testmaker.service.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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

//            remote.fetchUsers(local.userIds)?.let {
//                local.users = it
//                users?.postValue(it)
//                dirty = false
//            } ?: {
//                error.postValue(UserNotFoundException())
//            }()
        }
    }

    fun getQuestions(testId: Long): LiveData<ArrayList<Quest>> {
        if (questions == null) {
            questions = MutableLiveData()
            fetchQuestions(testId)
        }
        return questions as LiveData<ArrayList<Quest>>
    }

    private fun fetchQuestions(testId: Long) {
        GlobalScope.launch(Dispatchers.Main) {
                questions?.postValue(local.getQuestions(testId))
        }
    }

    fun clearQuestions() {
        questions = null
    }

}