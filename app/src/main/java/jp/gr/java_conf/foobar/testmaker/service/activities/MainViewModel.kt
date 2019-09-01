package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Cate
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class MainViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getTests(): List<Test> = repository.getTests()

    fun getNonCategorizedTests(): List<Test> = repository.getNonCategorizedTests()
    fun getExistingCategoryList(): List<Cate> = repository.getExistingCategoryList()
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



    val title: MutableLiveData<String> = MutableLiveData()
    var isEditing: MutableLiveData<Boolean> = MutableLiveData()

}