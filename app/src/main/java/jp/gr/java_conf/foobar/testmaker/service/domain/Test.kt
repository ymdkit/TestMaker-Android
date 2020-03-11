package jp.gr.java_conf.foobar.testmaker.service.domain

data class Test(
        val id: Long = -1,
        val color: Int,
        val limit: Int = 100,
        val startPosition: Int = 0,
        val title: String,
        val category: String = "",
        val history: Long = 0,
        val questions: List<Quest> = emptyList(),
        val documentId: String = "",
        val order: Int = 0
) {

    val questionsCorrectCount = questions.count { it.correct }

    companion object {
        fun createFromRealmTest(realmTest: RealmTest) = Test(
                realmTest.id,
                realmTest.color,
                realmTest.limit,
                realmTest.startPosition,
                realmTest.title ?: "",
                realmTest.getCategory(),
                realmTest.history,
                realmTest.questionsNonNull(),
                realmTest.documentId,
                realmTest.order
        )
    }

}