package jp.gr.java_conf.foobar.testmaker.service.models

data class LocalQuestion(var type: Int = 0,
                         var question: String = "",
                         var answer: String = "",
                         var answers: Array<String> = arrayOf(""),
                         var others: Array<String> = arrayOf(""),
                         var imagePath: String = "",
                         var explanation: String = "",
                         var isAuto: Boolean = false,
                         var isCheckOrder: Boolean = false) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalQuestion

        if (!answers.contentEquals(other.answers)) return false
        if (!others.contentEquals(other.others)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = answers.contentHashCode()
        result = 31 * result + others.contentHashCode()
        return result
    }
}