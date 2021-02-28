package me.digi.ongoingpostbox.utils

sealed class Resource<out T>(val data: T? = null, val message: String? = null) {
    class Loading<out T> : Resource<T>()
    class Success<out T>(data: T) : Resource<T>(data)
    class Failure<out T>(message: String?, data: T? = null) : Resource<T>(data, message)
}
