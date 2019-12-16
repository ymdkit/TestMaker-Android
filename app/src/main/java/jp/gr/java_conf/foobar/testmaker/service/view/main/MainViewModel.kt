package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class MainViewModel(private val repository: TestMakerRepository, private val auth: Auth, private val preference: SharedPreferenceManager) : ViewModel() {

    fun getExistingCategoryList(): LiveData<List<Cate>> = repository.getExistingCategoriesOfLiveData()
    fun getCategories(): List<Cate> = repository.getCategories()
    fun addCategory(category: Cate) = repository.addCategory(category)
    fun deleteCategory(category: Cate) = repository.deleteCategory(category)
    fun addTest(title: String, colorId: Int, category: String) {
        val test = Test()
        test.title = title
        test.color = colorId
        test.setCategory(category)
        repository.addOrUpdateTest(test)
    }

    fun addOrUpdateTest(test: Test): Long = repository.addOrUpdateTest(test)
    fun getMaxQuestionId(): Long = repository.getMaxQuestionId()
    suspend fun downloadTest(testId: String): FirebaseTestResult = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun getAuthUIIntent(): Intent = auth.getAuthUIIntent()
    fun getUser(): FirebaseUser? = auth.getUser()
    fun createUser(user: FirebaseUser?) = repository.setUser(user)
    fun getTests(): LiveData<List<Test>> = repository.getTestsOfLiveData()
    fun fetchTests() = repository.fetchTests()
    fun fetchCategories() = repository.fetchCategories()
    fun sortTests(from: Long, to: Long) = repository.sortTests(from, to)
    fun changeCategory(testId: Long, category: String) = repository.changeCategory(testId, category)
    fun sortAllTests(mode: Int) = repository.sortAllTests(mode)

    fun migrateSortSetting() {
        repository.sortAllTests(preference.sort)
        preference.sort = -1
    }

    val title: MutableLiveData<String> = MutableLiveData()
    var isEditing: MutableLiveData<Boolean> = MutableLiveData()
}