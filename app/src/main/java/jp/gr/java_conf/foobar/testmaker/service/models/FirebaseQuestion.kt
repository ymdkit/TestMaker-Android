package jp.gr.java_conf.foobar.testmaker.service.models

data class FirebaseQuestion(val question: String = "",
                            val answer: String = "",
                            val answers: List<String> = emptyList(),
                            val others: List<String> = emptyList(),
                            val explanation: String = "",
                            val imageRef: String = "",
                            val type: Int = 0,
                            val isAuto: Boolean = false,
                            val isCheckOrder: Boolean = false,
                            val order: Int = 0
) {

    fun toQuest(): Quest {

        val quest = Quest()
        quest.problem = question

        if (answer.isNotEmpty()) {
            quest.answer = answer
        } else {
            answers.toTypedArray().forEach { quest.answer += "$it " }
        }
        quest.setAnswers(answers.toTypedArray())
        quest.setSelections(others.toTypedArray())
        quest.auto = isAuto
        quest.isCheckOrder = isCheckOrder
        quest.imagePath = imageRef
        quest.explanation = explanation
        quest.type = type

        return quest
    }

}
