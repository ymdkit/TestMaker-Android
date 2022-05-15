package com.example.core

sealed class QuestionImage {
    object Empty : QuestionImage()
    data class FireStoreImage(val ref: String) : QuestionImage()
    data class LocalImage(val path: String) : QuestionImage()

    companion object {
        fun fromRawString(rawString: String) =
            if (rawString.isNotEmpty()) {
                if (rawString.contains("/")) {
                    FireStoreImage(ref = rawString)
                } else {
                    LocalImage(path = rawString)
                }
            } else {
                Empty
            }
    }

    fun getRawString() =
        when (this) {
            is Empty -> ""
            is FireStoreImage -> ref
            is LocalImage -> path
        }
}