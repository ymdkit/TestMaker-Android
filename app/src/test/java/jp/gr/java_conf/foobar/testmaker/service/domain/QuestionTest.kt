package jp.gr.java_conf.foobar.testmaker.service.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionTest {

    @Test
    fun isCorrect() {
        val question = Question(answers = listOf("a", "b", "c"))
        assertTrue(question.isCorrect(listOf("a", "b", "c"))) //正解
        assertTrue(question.isCorrect(listOf("b", "a", "c"))) //順番変えても正解
        assertFalse(question.copy(isCheckOrder = true).isCorrect(listOf("b", "a", "c"))) //順序変えると不正解

        assertFalse(question.isCorrect(listOf("a", "a", "a"))) //同じ答え複数はダメ
        assertFalse(question.copy(isCheckOrder = true).isCorrect(listOf("a", "a", "a"))) //同じ答え複数はダメ
        assertFalse(question.isCorrect(listOf("a", "b"))) //足りない
        assertFalse(question.copy(isCheckOrder = true).isCorrect(listOf("a", "b"))) //足りない

        assertTrue(question.copy(answers = listOf("a", "a", "b"), isCheckOrder = true).isCorrect(listOf("a", "a", "b")))//順序指定時は同じ回答複数でもOK


        assertFalse(question.isCorrect(listOf("A", "b", "c")))
        assertFalse(question.copy(isCheckOrder = true).isCorrect(listOf("A", "b", "c")))
        assertTrue(question.isCorrect(listOf("A", "b", "c"), true))
        assertTrue(question.copy(isCheckOrder = true).isCorrect(listOf("A", "b", "c"), true))
    }
}