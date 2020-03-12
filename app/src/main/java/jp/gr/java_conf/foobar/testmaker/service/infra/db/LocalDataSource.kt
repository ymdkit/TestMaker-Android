package jp.gr.java_conf.foobar.testmaker.service.infra.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import java.io.FileNotFoundException
import java.io.IOException

class LocalDataSource(private val realm: Realm, private val preference: SharedPreferenceManager, private val context: Context) {

    fun getTest(testId: Long): RealmTest {
        return realm.where(RealmTest::class.java).equalTo("id", testId).findFirst() ?: RealmTest()
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
        test.id = realm.where(RealmTest::class.java).max("id")?.toLong()?.plus(1) ?: 1
        test.order = test.id.toInt()

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

    fun updateDocumentId(test: RealmTest, documentId: String) {
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