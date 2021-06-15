package me.digi.sdk.saas.utils

inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> = try {
    action()
} catch (exception: Exception) {
    Resource.Failure(exception.localizedMessage)
}