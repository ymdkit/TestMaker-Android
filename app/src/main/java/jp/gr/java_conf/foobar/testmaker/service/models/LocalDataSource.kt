package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmList
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.activities.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

class LocalDataSource(private val realm: Realm, private val preference: SharedPreferenceManager,private val context: Context) {


    fun getTests(): List<Test>? {
        return emptyList()
    }

    private fun getTest(testId: Long): Test {
        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()

    }

    fun getQuestions(testId: Long): ArrayList<Quest>? {

        val realmArray = getTest(testId).getQuestions()
        return ArrayList(realmArray)
    }

    fun addQuestion(testId: Long, localQuestion: LocalQuestion, questionId: Long) {
        realm.beginTransaction()

        val test = getTest(testId)
        val question: Quest?

        if (questionId != -1L) {
            question = realm.where(Quest::class.java).equalTo("id", questionId).findFirst()
            if (question == null) {
                Toast.makeText(context, context.getString(R.string.msg_already_delete), Toast.LENGTH_SHORT).show()
                realm.commitTransaction()
                return
            }
        } else {
            // 初期化
            var nextUserId: Long
            nextUserId = 1
            // userIdの最大値を取得
            val maxUserId = realm.where(Quest::class.java).max("id")
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxUserId != null) {
                nextUserId = (maxUserId.toInt() + 1).toLong()
            }

            question = realm.createObject(Quest::class.java, nextUserId) ?: Quest()
            question.order = test.getQuestions().size
            test.addQuestion(question)
        }

        question.explanation = localQuestion.explanation
        question.type = localQuestion.type
        question.problem = localQuestion.question
        question.answer = localQuestion.answer
        question.setSelections(localQuestion.others)
        question.setAnswers(localQuestion.answers)
        question.correct = false
        question.auto = localQuestion.isAuto
        question.isCheckOrder = localQuestion.isCheckOrder

        if (question.imagePath != localQuestion.imagePath) {

            context.deleteFile(question.imagePath)

        }
        question.imagePath = localQuestion.imagePath

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

    fun saveImage(fileName: String,bitmap: Bitmap) {
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

    fun convert(structTest: StructTest,testId: Long) {
        realm.beginTransaction()

        // 初期化
        var nextUserId = 1
        // userIdの最大値を取得
        val maxUserId = realm.where(Test::class.java).max("id")
        // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
        if (maxUserId != null) {
            nextUserId = maxUserId.toInt() + 1
        }

        val test: Test // Create managed objects directly

        if (testId != -1L) {

            test = getTest(testId)
            test.setQuestions(RealmList())

        } else {

            test = realm.createObject(Test::class.java, nextUserId) // Create managed objects directly

        }

        test.title = structTest.title
        test.color = structTest.color
        test.setCategory(structTest.category ?: "")
        test.history = structTest.history
        test.limit = 100

        for (j in 0 until structTest.problems.size) {

            // 初期化
            var nextQuestId: Int? = 1
            // userIdの最大値を取得
            val maxQuestId = realm.where(Quest::class.java).max("id")
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxQuestId != null) {
                nextQuestId = maxQuestId.toInt() + 1
            }

            val q = realm.createObject(Quest::class.java, nextQuestId)

            q.problem = structTest.problems[j].question
            q.answer = structTest.problems[j].answer
            q.auto = structTest.problems[j].auto
            q.isCheckOrder = structTest.problems[j].isCheckOrder
            q.type = structTest.problems[j].type
            q.setSelections(structTest.problems[j].others)
            q.setAnswers(structTest.problems[j].answers)
            q.explanation = structTest.problems[j].explanation
            q.imagePath = structTest.problems[j].imagePath
            q.order = j

            test.addQuestion(q)
        }

        realm.commitTransaction()
    }


}