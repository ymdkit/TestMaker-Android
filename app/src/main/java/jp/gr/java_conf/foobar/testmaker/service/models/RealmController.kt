package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.realm.*
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import java.util.*

/**
 * Created by keita on 2017/02/08.
 */

class RealmController(private val context: Context, config: RealmConfiguration) {
    private val realm: Realm = Realm.getInstance(config)

    private val sharedPreferenceManager: SharedPreferenceManager = SharedPreferenceManager(context)

    val list: ArrayList<Test>
        get() {

            val realmArray: RealmResults<Test>

            when (sharedPreferenceManager.sort) {
                -1 ->

                    realmArray = realm.where(Test::class.java).findAll().sort("title")
                0 ->

                    realmArray = realm.where(Test::class.java).findAll().sort("title", Sort.DESCENDING)
                1 ->

                    realmArray = realm.where(Test::class.java).findAll().sort("title")
                2 ->

                    realmArray = realm.where(Test::class.java).findAll().sort("history", Sort.DESCENDING)

                else -> realmArray = realm.where(Test::class.java).findAll().sort("title")
            }

            return ArrayList(realmArray)
        }

    val listNotEmpty: ArrayList<Test>
        get() {
            return ArrayList(list.filter { it.getQuestionsForEach().size > 0 })

        }

    val cateList: ArrayList<Cate>
        get() {
            return ArrayList(realm.where(Cate::class.java).findAll().sort("category"))
        }

    val existingCateList: ArrayList<Cate>
        get() {
            val items = ArrayList<Cate>()

            for (cate in cateList) {
                for (test in list) {
                    if (cate.category == test.getCategory()) {
                        items.add(cate)
                        break
                    }
                }
            }
            return items
        }

    val nonCategorizedTests: ArrayList<Test>
        get() {

            val items = ArrayList<Test>()

            outside@ for (test in list) {

                for (cate in cateList) {
                    if (cate.category == test.getCategory()) {
                        continue@outside
                    }
                }
                items.add(test)
            }
            return items
        }


    fun getTest(testId: Long): Test {

        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()
    }

    fun addTest(title: String, color: Int, category: String): Long {

        realm.beginTransaction()

        // 初期化
        var nextUserId: Long = 1
        // userIdの最大値を取得
        val maxUserId = realm.where(Test::class.java).max("id")
        // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
        if (maxUserId != null) {
            nextUserId = (maxUserId.toInt() + 1).toLong()
        }

        val test = realm.createObject(Test::class.java, nextUserId)

        test.title = title
        test.color = color
        test.setCategory(category)
        test.limit = 100

        realm.commitTransaction()

        return nextUserId
    }


    fun updateTest(test: Test, title: String, color: Int, category: String) {
        realm.beginTransaction()

        test.title = title
        test.color = color
        test.setCategory(category)

        realm.commitTransaction()
    }

    fun deleteTest(test: Test) {

        realm.beginTransaction()

        test.deleteFromRealm()

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

    fun addCate(category: String, color: Int) {

        realm.beginTransaction()

        val cate = realm.createObject(Cate::class.java)

        cate.category = category
        cate.color = color

        realm.commitTransaction()

    }

    fun deleteCate(cate: Cate) {

        realm.beginTransaction()

        cate.deleteFromRealm()

        realm.commitTransaction()
    }

    fun getQuestions(testId: Long): ArrayList<Quest> {

        val realmArray = getTest(testId).getQuestions()

        return ArrayList(realmArray)
    }

    fun getQuestionsSolved(testId: Long): ArrayList<Quest> {

        val array = ArrayList<Quest>()

        val realmArray = getTest(testId).getQuestions()

        for (quest in realmArray) {

            if (quest.solving) {
                array.add(quest)
            }
        }

        return array
    }

    fun addQuestions(testId: Long, questions: Array<Quest>) {

        val test = getTest(testId)

        realm.beginTransaction()

        questions.forEach {
            // 初期化
            var nextUserId: Long
            nextUserId = 1
            // userIdの最大値を取得
            val maxUserId = realm.where(Quest::class.java).max("id")
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxUserId != null) {
                nextUserId = (maxUserId.toInt() + 1).toLong()
            }

            val question = realm.createObject(Quest::class.java, nextUserId) ?: Quest()

            question.explanation = it.explanation
            question.type = it.type
            question.problem = it.problem
            question.answer = it.answer
            question.selections = it.selections
            question.answers = it.answers
            question.correct = false
            question.auto = it.auto
            question.imagePath = it.imagePath
            question.order = test.getQuestions().size

            test.addQuestion(question)
        }

        realm.commitTransaction()

    }

    fun addQuestion(testId: Long, problem: LocalQuestion, questionId: Long) {

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

        question.explanation = problem.explanation
        question.type = problem.type
        question.problem = problem.question
        question.answer = problem.answer
        question.setSelections(problem.others)
        question.setAnswers(problem.answers)
        question.correct = false
        question.auto = problem.isAuto
        question.isCheckOrder = problem.isCheckOrder

        if (question.imagePath != problem.imagePath) {

            context.deleteFile(question.imagePath)

        }
        question.imagePath = problem.imagePath

        realm.commitTransaction()

        Toast.makeText(context, context.getString(R.string.msg_save), Toast.LENGTH_LONG).show()

    }

    fun updateCorrect(quest: Quest, correct: Boolean) {

        realm.beginTransaction()

        quest.correct = correct

        realm.commitTransaction()

    }

    fun updateSolving(questionId: Long, solving: Boolean) {
        realm.beginTransaction()

        val question = realm.where(Quest::class.java).equalTo("id", questionId).findFirst()
                ?: Quest()

        question.solving = solving

        realm.commitTransaction()
    }

    private fun updateOrder(questionId: Long, order: Int) {
        realm.beginTransaction()

        val question = realm.where(Quest::class.java).equalTo("id", questionId).findFirst()
                ?: Quest()

        question.order = order

        realm.commitTransaction()
    }


    fun close() {

        realm.close()

    }

    fun convert(structTest: StructTest, testId: Long) { //structTest を　Test に変換

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


    fun getCategorizedList(category: String): ArrayList<Test> {

        val array = ArrayList<Test>()

        val realmArray: RealmResults<Test>

        when (sharedPreferenceManager.sort) {
            -1 ->

                realmArray = realm.where(Test::class.java).findAll().sort("title")
            0 ->

                realmArray = realm.where(Test::class.java).findAll().sort("title")
            1 ->

                realmArray = realm.where(Test::class.java).findAll().sort("title", Sort.DESCENDING)
            2 ->

                realmArray = realm.where(Test::class.java).findAll().sort("history", Sort.DESCENDING)

            else -> realmArray = realm.where(Test::class.java).findAll().sort("title")
        }

        for (test in realmArray) {
            if (test.getCategory() == category) {
                array.add(test)
            }
        }

        return array

    }

    fun sortManual(from: Int, to: Int, testId: Long) {

        val questions = getTest(testId).getQuestions()

        val fromOrder = questions[from]?.order ?: 0
        val toOrder = questions[to]?.order ?: 0

        updateOrder(questions[from]?.id ?: -1L, toOrder)
        updateOrder(questions[to]?.id ?: -1L, fromOrder)

    }

    fun migrateOrder(testId: Long) {

        val questions = getTest(testId).getQuestions()

        if (questions.size < 2) return

        realm.beginTransaction()

        if (questions[0]?.order == questions[1]?.order) {

            getTest(testId).getQuestionsForEach().forEachIndexed { index, quest -> quest.order = index }

        }

        realm.commitTransaction()

    }

    fun resetSolving(testId: Long) {

        realm.beginTransaction()

        getTest(testId).getQuestionsForEach().forEach { it.solving = false }

        realm.commitTransaction()

    }

    fun removeQuestions(id: Long, checkBoxStates: Array<Boolean>) { //移動後に元の問題集からは取り除く

        val test = getTest(id)

        realm.beginTransaction()

        val list = RealmList<Quest>()

        test.getQuestions().filterIndexed { index, quest -> !checkBoxStates[index] }.forEach { list.add(it) }

        test.setQuestions(list)

        realm.commitTransaction()

    }

    fun resetAchievement(testId: Long) {

        realm.beginTransaction()

        getTest(testId).resetAchievement()

        realm.commitTransaction()


    }


}
