package io.jasonatwood.networkmonkey

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response

class Operation(private val description: String,
                private val method: Method,
                private val httpUrlToMatch: HttpUrl?,
                internal val weight: Int,
                internal val mandatory: Boolean = false,
                private val requestMonkey: ((initialRequest: Request) -> Request)?,
                private val responseMonkey: ((initialResponse: Response) -> Response)?) {

    fun monkeyWithRequest(initialRequest: Request): Request {
        if (!matches(initialRequest)) {
            return initialRequest
        }

        requestMonkey?.let {
            log(description, initialRequest)
            return it.invoke(initialRequest)
        } ?: run {
            return initialRequest
        }
    }

    fun monkeyWithResponse(initialResponse: Response): Response {
        if (!matches(initialResponse.request())) {
            return initialResponse
        }

        responseMonkey?.let {
            log(description, initialResponse.request())
            return it.invoke(initialResponse)
        } ?: run {
            return initialResponse
        }
    }

    fun matches(request: Request): Boolean {
        if (method != Method.ANY && request.method() != method.okhttpVerb) {
            return false
        }

        if (httpUrlToMatch != null && request.url() != httpUrlToMatch) {
            return false
        }

        return true
    }

    private fun log(description: String, request: Request) {
        println("Performing monkey operation: $description on: "
                + request.method() + " " + request.url())
    }

    enum class Method(val okhttpVerb: String) {
        GET("GET"),
        POST("POST"),
        PATCH("PATCH"),
        CREATE("CREATE"),
        DELETE("DELETE"),
        PUT("PUT"),
        ANY(""),
    }
}