package me.digi.sdk.utilities

import android.net.Uri
import java.lang.IllegalArgumentException

class DMEDeepLinkBuilder {

    companion object {
        private const val digimeScheme = "digime"
    }

    private var action: String? = null
    private var parameters: Map<String, String>? = null

    fun setAction(action: String): DMEDeepLinkBuilder {
        this.action = action
        return this
    }

    fun setParameters(parameters: Map<String, String>): DMEDeepLinkBuilder {
        this.parameters = parameters
        return this
    }

    fun addParameter(key: String, value: String): DMEDeepLinkBuilder {
        if (parameters == null)
            setParameters(emptyMap())

        var existingParams = parameters!!.toMutableMap()

        if (existingParams[key] != null)
            throw IllegalArgumentException("Key \"$key\" already exists in parameters.")

        existingParams[key] = value
        setParameters(existingParams)

        return this
    }

    fun build(): Uri {

        val host = action ?: ""

        val queryString = parameters?.let { params ->

            params
                .map { "${it.key}=${it.value}" }
                .joinToString("&", "?")

        } ?: ""

        return Uri.parse("$digimeScheme://$host$queryString")
    }
}