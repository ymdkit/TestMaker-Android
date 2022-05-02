package com.example.usecase.utils

sealed class Resource<out T> {
    object Empty : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<T>(val value: T) : Resource<T>()
    data class Failure(val message: String) : Resource<Nothing>()

    fun <U> map(block: (T) -> U): Resource<U> =
        when (this) {
            is Success -> Success(block(this.value))
            is Empty -> Empty
            is Loading -> Loading
            is Failure -> Failure(this.message)
        }

    fun getOrNull(): T? =
        when (this) {
            is Success -> this.value
            else -> null
        }
}
