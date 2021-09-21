package me.digi.saas.utils

/**
 * Utility class that helps us handle UI components
 */
sealed class Resource<out T>(val data: T? = null, val message: String? = null) {
    class Idle<out T> : Resource<T>()
    class Loading<out T> : Resource<T>()
    class Success<out T>(data: T) : Resource<T>(data)
    class Failure<out T>(message: String?, data: T? = null) : Resource<T>(data, message)
}