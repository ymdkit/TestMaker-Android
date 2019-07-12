package jp.gr.java_conf.foobar.testmaker.service.models

import jp.gr.java_conf.foobar.testmaker.service.Constants

data class FirebaseQuestion(val question: String = "",
                            val answer: String = "",
                            val answers: List<String> = emptyList(),
                            val others: List<String> = emptyList(),
                            val explanation: String = "",
                            val imageRef: String = "",
                            val type: Int = 0,
                            val isAuto: Boolean = false,
                            val isCheckOrder: Boolean = false,
                            val ownerId: String = ""
) {

    fun toStructQuestion(): StructQuestion {

        var q: StructQuestion? = null

        when (type) {
            Constants.WRITE -> {
                q = StructQuestion(question, answer)
            }
            Constants.SELECT -> {
                q = StructQuestion(question, answer, others.toTypedArray())
            }
            Constants.COMPLETE -> {
                q = StructQuestion(question, answers.toTypedArray())
            }
            Constants.SELECT_COMPLETE -> {
                q = StructQuestion(question, answers.toTypedArray(), others.toTypedArray())
            }
        }

        q?.let {
            q.auto = isAuto
            q.imagePath = imageRef
            q.explanation = explanation
            q.isCheckOrder = q.isCheckOrder

            return q
        }

        return StructQuestion("", "")
    }

}
