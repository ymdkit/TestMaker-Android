package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

class LocalDataSource(private val realm: Realm, private val context: Context) {


    fun getTests(): List<Test>? {
        return emptyList()
    }

    fun getQuestions(testId: Long): ArrayList<Quest>? {

        val realmArray = getTest(testId).getQuestions()
        return ArrayList(realmArray)
    }

    private fun getTest(testId: Long): Test {
        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()

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


}