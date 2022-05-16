package com.example.core.utils

inline fun <T> Iterable<T>.allIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    var index = 0
    for (element in this) if (!predicate(index++,element)) return false
    return true
}

fun <T> Iterable<T>.replaced(index: Int, element: T) =
    mapIndexed{ i, t -> if(index == i) element else t }