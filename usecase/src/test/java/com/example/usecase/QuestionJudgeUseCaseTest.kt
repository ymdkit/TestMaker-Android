package com.example.usecase

import com.example.core.QuestionType
import com.example.usecase.model.QuestionUseCaseModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class QuestionJudgeUseCaseTest {

    private lateinit var target: QuestionJudgeUseCase

    @Before
    fun setup() {
        target = QuestionJudgeUseCase(mockk())
    }

    @Test
    fun judgeSingleAnswer() {
        val question = mockk<QuestionUseCaseModel>()
        every { question.problem } returns "problem"
        every { question.answers } returns listOf("answer")
        every { question.type } returns QuestionType.WRITE
        every { question.isCheckAnswerOrder } returns false

        // 答え単一 正解
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 入れ替え
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 大文字区別なし
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 入れ替え かつ 大文字区別なし
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )

        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
    }

    @Test
    fun judgeSelectSingleAnswer() {
        val question = mockk<QuestionUseCaseModel>()
        every { question.problem } returns "problem"
        every { question.answers } returns listOf("answer")
        every { question.type } returns QuestionType.SELECT
        every { question.isCheckAnswerOrder } returns false

        // 答え単一 正解
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 入れ替え
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 大文字区別なし
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 入れ替え かつ 大文字区別なし
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（別の文字列）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("b"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（大文字小文字）
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("Answer"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )

        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = true
            )
        )
        // 答え単一 不正解（問題文）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("Problem"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = true
            )
        )
    }

    @Test
    fun judgeCompleteMultipleAnswerNoOrder() {
        val question = mockk<QuestionUseCaseModel>()
        every { question.problem } returns "problem"
        every { question.answers } returns listOf("answer1", "answer2")
        every { question.type } returns QuestionType.COMPLETE
        every { question.isCheckAnswerOrder } returns false

        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 順番違う
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer2", "answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 足りない
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 同じ解答を連打している
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 過剰
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2", "answer3"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 数は同じだが内容が違う
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer3", "answer4"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 問題、回答逆転（不正解）
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer3", "answer4"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )

        // 問題、回答逆転
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("question"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )

    }

    @Test
    fun judgeSelectCompleteMultipleAnswerNoOrder() {
        val question = mockk<QuestionUseCaseModel>()
        every { question.problem } returns "problem"
        every { question.answers } returns listOf("answer1", "answer2")
        every { question.type } returns QuestionType.SELECT_COMPLETE
        every { question.isCheckAnswerOrder } returns false

        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 順番違う
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer2", "answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 足りない
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 同じ解答を連打している
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 過剰
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2", "answer3"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 数は同じだが内容が違う
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer3", "answer4"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 問題、解答逆転
        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )

        // 問題、回答逆転
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("question"),
                isSwapProblemAndAnswer = true,
                isCaseInsensitive = false
            )
        )

    }

    @Test
    fun judgeMultipleAnswerOrdered() {
        val question = mockk<QuestionUseCaseModel>()
        every { question.problem } returns "problem"
        every { question.answers } returns listOf("answer1", "answer2")
        every { question.isCheckAnswerOrder } returns true

        Assert.assertTrue(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 順番違う
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer2", "answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 足りない
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 同じ解答を連打している
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer1"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 過剰
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer1", "answer2", "answer3"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

        // 数は同じだが内容が違う
        Assert.assertFalse(
            target.judge(
                expect = question,
                actual = listOf("answer3", "answer4"),
                isSwapProblemAndAnswer = false,
                isCaseInsensitive = false
            )
        )

    }
}