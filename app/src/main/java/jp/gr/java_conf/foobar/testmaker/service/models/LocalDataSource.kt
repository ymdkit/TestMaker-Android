package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.activities.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

class LocalDataSource(private val realm: Realm, private val preference: SharedPreferenceManager, private val context: Context) {

    fun getTests(): List<Test> = realm.copyFromRealm(when (preference.sort) {
        Constants.TITLE_DESCENDING ->
            realm.where(Test::class.java).findAll().sort("title", Sort.DESCENDING)
        Constants.TITLE_ASCENDING ->
            realm.where(Test::class.java).findAll().sort("title")
        Constants.HISTORY ->
            realm.where(Test::class.java).findAll().sort("history", Sort.DESCENDING)
        else ->
            realm.where(Test::class.java).findAll().sort("title")
    })


    private fun getTest(testId: Long): Test {
        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()
    }

    fun getQuestions(testId: Long): ArrayList<Quest>? {

        val realmArray = getTest(testId).questionsNonNull()
        return ArrayList(realmArray)
    }

    fun addQuestion(testId: Long, question: Quest, questionId: Long) {
        realm.beginTransaction()

        val test = getTest(testId)

        if (questionId != -1L) {
            question.id = questionId
            realm.copyToRealmOrUpdate(question)

        } else {

            question.id = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 1
            realm.copyToRealmOrUpdate(question)
            test.addQuestion(question)
        }

        realm.commitTransaction()
    }

    fun deleteQuestion(question: Quest) {
        realm.beginTransaction()
        question.deleteFromRealm()
        realm.commitTransaction()
    }

    fun loadImage(imagePath: String, setImage: (Bitmap) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                val imageOptions = BitmapFactory.Options()
                imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
                try {

                    val input = context.openFileInput(imagePath)
                    val bm = BitmapFactory.decodeStream(input, null, imageOptions)

                    input.close()

                    return@withContext bm

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.let {
                if (it is Bitmap) setImage(it)
            }
        }
    }

    fun saveImage(fileName: String, bitmap: Bitmap) {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                val imageOptions = BitmapFactory.Options()
                imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
                try {

                    val outStream = context.openFileOutput(fileName, BaseActivity.MODE_PRIVATE)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                    outStream.close()

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun isAuto(): Boolean = preference.auto
    fun isCheckOrder(): Boolean = preference.isCheckOrder

    fun createObjectFromFirebase(firebaseTest: FirebaseTest) {
        realm.beginTransaction()

        val test = firebaseTest.toTest(context)
        test.id = realm.where(Test::class.java).max("id")?.toLong()?.plus(1) ?: 1

        firebaseTest.questions.forEachIndexed { index, it ->
            val question = it.toQuest()
            question.order = index
            question.id = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 1
            realm.copyToRealmOrUpdate(question)
            test.addQuestion(question)
        }

        realm.copyToRealmOrUpdate(test)

        realm.commitTransaction()
    }

    fun getNonCategorizedTests(): List<Test> = getTests().filter { !getCategories().map { cate -> cate.category }.contains(it.getCategory()) }

    fun getCategories(): List<Cate> = realm.copyFromRealm(realm.where(Cate::class.java).findAll().sort("category"))
            ?: emptyList()

    fun getExistingCategories(): List<Cate> = getCategories().filter { getTests().map { test -> test.getCategory() }.contains(it.category) }

    fun addCategory(category: Cate) {
        realm.beginTransaction()
        realm.copyToRealm(category)
        realm.commitTransaction()
    }

    fun deleteCategory(category: Cate){
        realm.beginTransaction()
        realm.where(Cate::class.java).equalTo("category",category.category).findFirst()?.deleteFromRealm()
        realm.commitTransaction()
    }
}