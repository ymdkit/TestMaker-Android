package jp.gr.java_conf.foobar.testmaker.service.domain

import jp.gr.java_conf.foobar.testmaker.service.Constants
import org.junit.Assert.assertEquals
import org.junit.Test
import jp.gr.java_conf.foobar.testmaker.service.domain.Test as MyTest


class TestTest {

    @Test
    fun testRandomExtractedAnswers() {
        var test = MyTest(title = "", color = 0, source = "")
        assertEquals(emptyList<String>(), test.randomExtractedAnswers.sorted())

        // 一つ追加
        test = test.copy(questions = listOf(Question(answer = "answer1")))
        assertEquals(listOf("answer1"), test.randomExtractedAnswers.sorted())

        // 重複削除
        test = test.copy(questions = listOf(Question(answer = "answer1"), Question(answer = "answer1")))
        assertEquals(listOf("answer1"), test.randomExtractedAnswers.sorted())

        // 二つ追加
        test = test.copy(questions = listOf(Question(answer = "answer1"), Question(answer = "answer2")))
        assertEquals(listOf("answer1", "answer2"), test.randomExtractedAnswers.sorted())

        // 一つ追加（複数回答）
        test = test.copy(questions = listOf(Question(answers = listOf("answer1", "answer2"), type = Constants.SELECT_COMPLETE)))
        assertEquals(listOf("answer1", "answer2"), test.randomExtractedAnswers.sorted())

        // 重複削除（複数回答）
        test = test.copy(questions = listOf(Question(answers = listOf("answer1", "answer2"), type = Constants.SELECT_COMPLETE), Question(answers = listOf("answer1", "answer2"), type = Constants.SELECT_COMPLETE)))
        assertEquals(listOf("answer1", "answer2"), test.randomExtractedAnswers.sorted())

        // 二つ追加（複数回答）
        test = test.copy(questions = listOf(Question(answers = listOf("answer1", "answer2"), type = Constants.SELECT_COMPLETE), Question(answers = listOf("answer3", "answer4"), type = Constants.SELECT_COMPLETE)))
        assertEquals(listOf("answer1", "answer2", "answer3", "answer4"), test.randomExtractedAnswers.sorted())

        // 複数形式の混合
        test = test.copy(questions = listOf(Question(answers = listOf("answer1", "answer2"), type = Constants.SELECT_COMPLETE), Question(answer = "answer3")))
        assertEquals(listOf("answer1", "answer2", "answer3"), test.randomExtractedAnswers.sorted())

        // 複数形式の混合かつ重複削除
        test = test.copy(questions = listOf(Question(answers = listOf("answer1", "answer2"), type = Constants.SELECT_COMPLETE), Question(answer = "answer2")))
        assertEquals(listOf("answer1", "answer2"), test.randomExtractedAnswers.sorted())
    }

    @Test
    fun testGetChoicesSelect() {
        var test = MyTest(title = "", color = 0, source = "")

        assertEquals(listOf("empty", "empty"), test.getChoices(2, "answer1", "empty"))

        test = test.copy(questions = listOf(Question(answer = "answer1")))
        assertEquals(listOf("answer1", "empty"), test.getChoices(2, "answer2", "empty"))
        assertEquals(listOf("empty", "empty"), test.getChoices(2, "answer1", "empty"))

    }

    @Test
    fun testGetChoicesSelectComplete() {
        var test = MyTest(title = "", color = 0, source = "")

        assertEquals(listOf("empty", "empty"), test.getChoices(2, listOf("answer1"), "empty"))

        test = test.copy(questions = listOf(Question(answer = "answer1")))
        assertEquals(listOf("answer1", "empty"), test.getChoices(2, listOf("answer2", "answer3"), "empty"))
        assertEquals(listOf("empty", "empty"), test.getChoices(2, listOf("answer1", "answer3"), "empty"))

        test = test.copy(questions = listOf(Question(answer = "answer1"), Question(answer = "answer2")))
        assertEquals(listOf("answer1", "answer2"), test.getChoices(2, listOf("answer3", "answer4"), "empty"))
        assertEquals(listOf("answer1", "empty"), test.getChoices(2, listOf("answer2", "answer3"), "empty"))
        assertEquals(listOf("empty", "empty"), test.getChoices(2, listOf("answer1", "answer2"), "empty"))

    }
}