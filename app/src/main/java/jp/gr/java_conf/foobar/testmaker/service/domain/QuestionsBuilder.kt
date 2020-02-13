package jp.gr.java_conf.foobar.testmaker.service.domain

class QuestionsBuilder(var questions: List<Quest>) {

    var isRetry = false

    fun retry(isRetry: Boolean): QuestionsBuilder {
        this.isRetry = isRetry
        if(isRetry) questions = questions.filter { it.solving }
        return this
    }

    fun startPosition(startPosition: Int): QuestionsBuilder{
        if(isRetry) return this
        questions = questions.drop(startPosition)
        return this
    }

    fun mistakeOnly(isMistakeOnly: Boolean): QuestionsBuilder {
        if(isMistakeOnly) questions = questions.filter { !it.correct }
        return this
    }

    fun shuffle(isRandom: Boolean): QuestionsBuilder {
        if(isRandom) questions = questions.shuffled()
        return this
    }

    fun limit(limit: Int): QuestionsBuilder {
        if(limit < questions.size) questions = questions.take(limit)
        return this
    }

    fun build() = questions
}