package com.example.usecase

import com.example.core.utils.allIndexed
import com.example.domain.repository.PreferenceRepository
import com.example.usecase.model.QuestionUseCaseModel
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionJudgeUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {

    fun judge(
        expect: QuestionUseCaseModel,
        actual: List<String>
    ): Boolean =
        judge(
            expect = expect,
            actual = actual,
            isSwapProblemAndAnswer = preferenceRepository.getAnswerSetting().isSwapProblemAndAnswer,
            isCaseInsensitive = preferenceRepository.getAnswerSetting().isCaseInsensitive
        )

    // todo テストを書く
    private fun judge(
        expect: QuestionUseCaseModel,
        actual: List<String>,
        isSwapProblemAndAnswer: Boolean,
        isCaseInsensitive: Boolean
    ): Boolean {
        var expectAnswerList = expect.answers
        var actualAnswerList = actual

        if (isSwapProblemAndAnswer) {
            expectAnswerList = listOf(expect.problem)
        }

        if (isCaseInsensitive) {
            expectAnswerList = expectAnswerList.map { it.lowercase(Locale.ENGLISH) }
            actualAnswerList = actualAnswerList.map { it.lowercase(Locale.ENGLISH) }
        }

        if (!expect.isCheckAnswerOrder) {
            expectAnswerList = expectAnswerList.sorted()
            actualAnswerList = actualAnswerList.sorted()
        }

        return expectAnswerList.allIndexed { index, it -> it == actualAnswerList[index] }
    }
}
