package jp.gr.java_conf.foobar.testmaker.service.domain

import org.junit.Assert
import org.junit.Test

class QuestionModelTest {

    private val mockQuestion = QuestionModel(
        id = 1L,
        problem = "問題文",
        answer = "解答",
        answers = listOf("解答1", "解答2", "解答3", "解答4"),
        wrongChoices = listOf("ハズレ1", "ハズレ2", "ハズレ3", "ハズレ4"),
        format = QuestionFormat.WRITE,
        explanation = "解説文",
        imageUrl = "",
        isCheckOrder = false,
        isAutoGenerateWrongChoices = false,
        isAnswering = true,
        answerStatus = AnswerStatus.UNANSWERED,
        order = 1,
    )

    @Test
    fun testGetProblem() {
        var question = mockQuestion
        question = question.copy(format = QuestionFormat.WRITE)
        Assert.assertEquals("問題文", question.getProblem(false))
        Assert.assertEquals("解答", question.getProblem(true))

        question = question.copy(format = QuestionFormat.SELECT)
        Assert.assertEquals("問題文", question.getProblem(false))
        Assert.assertEquals("問題文", question.getProblem(true))

        question = question.copy(format = QuestionFormat.COMPLETE)
        Assert.assertEquals("問題文", question.getProblem(false))
        Assert.assertEquals("解答1\n解答2\n解答3\n解答4", question.getProblem(true))

        question = question.copy(format = QuestionFormat.SELECT_COMPLETE)
        Assert.assertEquals("問題文", question.getProblem(false))
        Assert.assertEquals("問題文", question.getProblem(true))
    }

    @Test
    fun testGetAnswer() {
        var question = mockQuestion
        question = question.copy(format = QuestionFormat.WRITE)
        Assert.assertEquals("解答", question.getAnswer(false))
        Assert.assertEquals("問題文", question.getAnswer(true))

        question = question.copy(format = QuestionFormat.SELECT)
        Assert.assertEquals("解答", question.getAnswer(false))
        Assert.assertEquals("解答", question.getAnswer(true))

        question = question.copy(format = QuestionFormat.COMPLETE)
        Assert.assertEquals("", question.getAnswer(false))
        Assert.assertEquals("問題文", question.getAnswer(true))

        question = question.copy(format = QuestionFormat.SELECT_COMPLETE)
        Assert.assertEquals("", question.getAnswer(false))
        Assert.assertEquals("", question.getAnswer(true))
    }

    @Test
    fun testIsCorrect() {
        val question = mockQuestion.copy(answers = listOf("a", "b", "c"), format = QuestionFormat.COMPLETE)
        Assert.assertTrue(
            question.isCorrect(
                listOf("a", "b", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        ) //正解

        Assert.assertTrue(
            question.isCorrect(
                listOf("b", "a", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        ) //順番変えても正解

        Assert.assertFalse(
            question.copy(isCheckOrder = true).isCorrect(
                listOf("b", "a", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        ) //順序変えると不正解

        Assert.assertFalse(
            question.isCorrect(
                listOf("a", "a", "a"),
                isReverse = false,
                isCaseInsensitive = false
            )
        ) //同じ答え複数はダメ

        Assert.assertFalse(
            question.copy(isCheckOrder = true).isCorrect(
                listOf("a", "a", "a"),
                isReverse = false,
                isCaseInsensitive = false
            )
        ) //同じ答え複数はダメ
        Assert.assertFalse(
            question.isCorrect(
                listOf("a", "b"), isReverse = false,
                isCaseInsensitive = false
            )
        ) //足りない

        Assert.assertFalse(
            question.copy(isCheckOrder = true).isCorrect(
                listOf("a", "b"),
                isReverse = false,
                isCaseInsensitive = false
            )
        ) //足りない
        Assert.assertTrue(
            question.copy(answers = listOf("a", "a", "b"), isCheckOrder = true)
                .isCorrect(
                    listOf("a", "a", "b"),
                    isReverse = false,
                    isCaseInsensitive = false
                )
        )//順序指定時は同じ回答複数でもOK

        Assert.assertFalse(
            question.isCorrect(
                listOf("A", "b", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        )
        Assert.assertFalse(
            question.copy(isCheckOrder = true).isCorrect(
                listOf("A", "b", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        )
        Assert.assertTrue(
            question.isCorrect(
                listOf("A", "b", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        )
        Assert.assertTrue(
            question.copy(isCheckOrder = true).isCorrect(
                listOf("A", "b", "c"),
                isReverse = false,
                isCaseInsensitive = false
            )
        )
    }

}