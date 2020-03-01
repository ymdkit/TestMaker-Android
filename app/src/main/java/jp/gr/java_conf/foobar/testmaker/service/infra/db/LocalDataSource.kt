package jp.gr.java_conf.foobar.testmaker.service.infra.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.realm.Realm
import io.realm.RealmList
import io.realm.Sort
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import java.io.FileNotFoundException
import java.io.IOException

class LocalDataSource(private val realm: Realm, private val preference: SharedPreferenceManager, private val context: Context) {

    fun getTests(): List<Test> {
        val tests = realm.where(Test::class.java).findAll().sort("category", Sort.DESCENDING, "order", Sort.ASCENDING)
        realm.beginTransaction()
        tests.forEachIndexed { index, test ->
            test.order = index
        }
        realm.commitTransaction()
        return realm.copyFromRealm(realm.where(Test::class.java).findAll().sort("order"))
    }

    fun sortAllTests(mode: Int) {
        val tests = when (mode) {
            Constants.TITLE_DESCENDING ->
                realm.where(Test::class.java).findAll().sort("category", Sort.ASCENDING, "title", Sort.DESCENDING)
            Constants.TITLE_ASCENDING ->
                realm.where(Test::class.java).findAll().sort("category", Sort.ASCENDING, "title", Sort.ASCENDING)
            Constants.HISTORY ->
                realm.where(Test::class.java).findAll().sort("category", Sort.ASCENDING, "history", Sort.DESCENDING)
            else ->
                realm.where(Test::class.java).findAll().sort("category", Sort.ASCENDING, "title", Sort.ASCENDING)
        }

        realm.beginTransaction()
        tests.forEachIndexed { index, test ->
            test.order = index
        }
        realm.commitTransaction()
    }

    fun getTest(testId: Long): Test {
        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()
    }

    fun getTestClone(testId: Long): Test {
        return realm.copyFromRealm(realm.where(Test::class.java).equalTo("id", testId).findFirst()
                ?: Test())
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
            question.order = question.id.toInt()
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

    fun loadImage(imagePath: String): Bitmap? {
        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        try {

            val input = context.openFileInput(imagePath)
            val bm = BitmapFactory.decodeStream(input, null, imageOptions)

            input.close()

            return bm

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun saveImage(fileName: String, bitmap: Bitmap) {
        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        try {

            val outStream = context.openFileOutput(fileName, 0)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
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

    private fun getCategories(): List<Cate> = (realm.where(Cate::class.java).findAll().sort("category"))

    fun getCategoriesClone(): List<Cate> = realm.copyFromRealm(getCategories()).distinctBy { it.category }

    fun getExistingCategories(): List<Cate> = getCategoriesClone().filter { getTests().map { test -> test.getCategory() }.contains(it.category) }

    fun addCategory(category: Cate) {
        realm.beginTransaction()
        realm.copyToRealm(category)
        realm.commitTransaction()
    }

    fun deleteCategory(category: Cate) {
        realm.beginTransaction()
        realm.where(Cate::class.java).equalTo("category", category.category).findFirst()?.deleteFromRealm()
        realm.commitTransaction()
    }

    fun addOrUpdateTest(test: Test): Long {
        realm.beginTransaction()
        if (test.id == 0L) test.id = realm.where(Test::class.java).max("id")?.toLong()?.plus(1) ?: 1
        test.order = test.id.toInt()
        realm.copyToRealmOrUpdate(test)
        realm.commitTransaction()
        return test.id
    }

    fun updateTest(test: Test, title: String, color: Int, category: String) {
        realm.beginTransaction()

        test.title = title
        test.color = color
        test.setCategory(category)

        realm.commitTransaction()
    }

    fun sortTests(from: Long, to: Long) {

        val fromTest = getTest(from)
        val toTest = getTest(to)

        realm.beginTransaction()

        toTest.setCategory(fromTest.getCategory())

        val tmp = fromTest.order
        fromTest.order = toTest.order
        toTest.order = tmp

        realm.commitTransaction()

    }

    fun changeCategory(testId: Long, category: String) {
        val test = getTest(testId)

        val previousCategory = getExistingCategories()[getExistingCategories().indexOfFirst { it.category == category } - 1].category

        realm.beginTransaction()

        test.setCategory(if (test.getCategory() == previousCategory) "" else previousCategory)

        realm.commitTransaction()
    }

    fun deleteTest(test: Test) {

        realm.beginTransaction()
        test.deleteFromRealm()
        realm.commitTransaction()
    }

    fun addQuestions(testId: Long, questions: Array<Quest>) {
        questions.forEach { addQuestion(testId, it, -1L) }
    }

    fun deleteQuestions(testId: Long, array: Array<Boolean>) {
        val test = getTest(testId)

        realm.beginTransaction()
        val list = RealmList<Quest>()

        test.questionsNonNull().filterIndexed { index, quest -> !array[index] }
                .forEachIndexed { index, quest ->
                    quest.order = index
                    list.add(quest)
                }

        test.setQuestions(list)
        realm.commitTransaction()
    }

    fun resetAchievement(testId: Long) {
        realm.beginTransaction()
        getTest(testId).resetAchievement()
        realm.commitTransaction()
    }

    fun resetSolving(testId: Long) {
        realm.beginTransaction()
        getTest(testId).getQuestionsForEach().forEach { it.solving = false }
        realm.commitTransaction()
    }

    fun sortManual(from: Int, to: Int, testId: Long) {

        val questions = getTest(testId).questionsNonNull()
        val fromOrder = questions[from].order
        val toOrder = questions[to].order

        updateOrder(questions[from].id, toOrder)
        updateOrder(questions[to].id, fromOrder)

    }

    private fun updateOrder(questionId: Long, order: Int) {
        realm.beginTransaction()
        val question = realm.where(Quest::class.java).equalTo("id", questionId).findFirst()
                ?: Quest()
        question.order = order
        realm.commitTransaction()
    }

    fun migrateOrder(testId: Long) { //order実装前の問題集に対する処理

        val questions = getTest(testId).questionsNonNull()

        if (questions.size < 2) return

        realm.beginTransaction()

        if (questions[0].order == questions[1].order) {

            getTest(testId).getQuestionsForEach().forEachIndexed { index, quest -> quest.order = index }

        }
        realm.commitTransaction()
    }

    fun updateHistory(test: Test) {
        realm.beginTransaction()
        test.setHistory()
        realm.commitTransaction()
    }

    fun updateStart(test: Test, start: Int) {

        realm.beginTransaction()
        test.startPosition = start
        realm.commitTransaction()
    }

    fun updateLimit(test: Test, limit: Int) {

        realm.beginTransaction()
        test.limit = limit
        realm.commitTransaction()

    }

    fun updateCorrect(quest: Quest, correct: Boolean) {
        realm.beginTransaction()
        quest.correct = correct
        realm.commitTransaction()
    }

    fun updateSolving(quest: Quest, solving: Boolean) {
        realm.beginTransaction()
        quest.solving = solving
        realm.commitTransaction()

    }

    fun updateDocumentId(test: Test, documentId: String) {
        realm.beginTransaction()
        test.documentId = documentId
        realm.commitTransaction()
    }

    fun updateDocumentId(question: Quest, documentId: String) {
        realm.beginTransaction()
        question.documentId = documentId
        realm.commitTransaction()
    }

    fun getMaxQuestionId(): Long = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 1

}