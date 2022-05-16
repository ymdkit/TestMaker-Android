package com.example.core.utils

sealed class Resource<out T> {
    object Empty : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<T>(val value: T) : Resource<T>()
    data class Failure(val message: String) : Resource<Nothing>()

    companion object {
        fun <T, U, V> merge(
            resource1: Resource<T>,
            resource2: Resource<U>,
            transform: (T, U) -> V
        ): Resource<V> =
            when {
                resource1 is Failure && resource2 is Failure -> Failure(message = "${resource1.message}, ${resource2.message}")
                resource1 is Failure -> Failure(message = resource1.message)
                resource2 is Failure -> Failure(message = resource2.message)
                resource1 is Loading || resource2 is Loading -> Loading
                resource1 is Success && resource2 is Success -> Success(
                    value = transform(
                        resource1.value,
                        resource2.value
                    )
                )
                else -> Empty
            }
    }

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
