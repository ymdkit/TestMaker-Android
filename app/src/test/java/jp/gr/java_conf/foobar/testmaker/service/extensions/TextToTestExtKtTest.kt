package jp.gr.java_conf.foobar.testmaker.service.extensions

import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TextToTestExtKtTest {


    @Test
    fun toTest() {

        runBlocking {

            val context = RuntimeEnvironment.application

            var line = "${context.getString(R.string.load_short_answers)},記述問題の問題文,記述問題の答え\n${context.getString(R.string.load_explanation)},解説文"
            var test = line.toTest(context)

            assertEquals("記述問題の問題文", "記述問題の問題文", test.questions[0].question)
            assertEquals("記述問題の解答", "記述問題の答え", test.questions[0].answer)
            assertEquals("記述問題の形式", Constants.WRITE, test.questions[0].type)
            assertEquals("記述問題の解答（複数）", 0, test.questions[0].answers.size)
            assertEquals("記述問題の選択肢（複数）", 0, test.questions[0].others.size)
            assertEquals("記述問題の解説", "解説文", test.questions[0].explanation)


            line = "${context.getString(R.string.load_multiple_answers)},完答問題の問題文,答え1,答え2,答え3,答え4"
            test = line.toTest(context)

            assertEquals("完答問題の問題文", "完答問題の問題文", test.questions[0].question)
            assertEquals("完答問題の解答", "答え1 答え2 答え3 答え4", test.questions[0].answer)
            assertEquals("完答問題の形式", Constants.COMPLETE, test.questions[0].type)
            assertEquals("完答問題の解答（複数）", 4, test.questions[0].answers.size)
            test.questions[0].answers.forEachIndexed { index, select ->
                assertEquals("完答問題の解答", "答え${index + 1}", select)
            }
            assertEquals("完答問題の選択肢（複数）", 0, test.questions[0].others.size)
            assertEquals("完答問題の解説", "", test.questions[0].explanation)
            assertEquals("完答問題の解答順", false, test.questions[0].isCheckOrder)


            line = "${context.getString(R.string.load_multiple_answers_order)},完答問題の問題文,答え1,答え2,答え3,答え4"
            test = line.toTest(context)
            assertEquals("完答問題の解答順", true, test.questions[0].isCheckOrder)


            line = "${context.getString(R.string.load_selection_problems)},選択問題の問題文,選択問題の答え,外れ1,外れ2,外れ3,外れ4"
            test = line.toTest(context)

            assertEquals("選択問題の問題文", "選択問題の問題文", test.questions[0].question)
            assertEquals("選択問題の解答", "選択問題の答え", test.questions[0].answer)
            assertEquals("選択問題の形式", Constants.SELECT, test.questions[0].type)
            assertEquals("選択問題の解答（複数）", 0, test.questions[0].answers.size)
            assertEquals("選択問題の選択肢（複数）", 4, test.questions[0].others.size)
            test.questions[0].others.forEachIndexed { index, select ->
                assertEquals("選択問題の選択肢", "外れ${index + 1}", select)
            }
            assertEquals("選択問題の解説", "", test.questions[0].explanation)
            assertEquals("選択問題の自動生成", false, test.questions[0].isAutoGenerateOthers)

            line = "${context.getString(R.string.load_selection_auto_problems)},選択問題の問題文,選択問題の答え,5"
            test = line.toTest(context)

            assertEquals("選択問題の選択肢（複数）", 5, test.questions[0].others.size)
            assertEquals("選択問題の自動生成", true, test.questions[0].isAutoGenerateOthers)


            line = "${context.getString(R.string.load_select_complete_problem)},選択完答問題の問題文,2,4,答え1,答え2,外れ1,外れ2,外れ3,外れ4"
            test = line.toTest(context)

            assertEquals("選択完答問題の問題文", "選択完答問題の問題文", test.questions[0].question)
            assertEquals("選択完答問題の解答", "答え1 答え2", test.questions[0].answer)
            assertEquals("選択完答問題の形式", Constants.SELECT_COMPLETE, test.questions[0].type)
            assertEquals("選択完答問題の解答（複数）", 2, test.questions[0].answers.size)
            test.questions[0].answers.forEachIndexed { index, select ->
                assertEquals("選択完答問題の解答", "答え${index + 1}", select)
            }
            assertEquals("選択完答問題の選択肢（複数）", 4, test.questions[0].others.size)
            test.questions[0].others.forEachIndexed { index, select ->
                assertEquals("選択完答問題の選択肢", "外れ${index + 1}", select)
            }
            assertEquals("選択完答問題の解説", "", test.questions[0].explanation)
            assertEquals("選択問題の自動生成", false, test.questions[0].isAutoGenerateOthers)

            line = "${context.getString(R.string.load_select_complete_auto_problem)},選択完答問題の問題文,2,答え1,答え2"
            test = line.toTest(context)

            assertEquals("選択問題の自動生成", true, test.questions[0].isAutoGenerateOthers)
            assertEquals("選択完答問題の解答（複数）", 2, test.questions[0].answers.size)
            assertEquals("選択完答問題の選択肢（複数）", 2, test.questions[0].others.size)

            line = "${context.getString(R.string.load_title)},タイトル\n${context.getString(R.string.load_category)},カテゴリ\n${context.getString(R.string.load_color)},12345"
            test = line.toTest(context)

            assertEquals("問題集のタイトル", "タイトル", test.title)
            assertEquals("問題集のカテゴリ", "カテゴリ", test.category)
            assertEquals("問題集の色", 12345, test.color)

        }
    }
}