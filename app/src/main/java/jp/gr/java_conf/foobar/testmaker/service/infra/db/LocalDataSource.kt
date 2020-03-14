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
}