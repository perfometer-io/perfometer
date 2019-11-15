package io.perfometer.http

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
}

data class HttpStatus(val code: Int) {

    val generalError: Boolean
        get() = code == -1

    val isInformative: Boolean
        get() = isCodeIn(100..199)

    val isSuccess: Boolean
        get() = isCodeIn(200..299)

    val isRedirect: Boolean
        get() = isCodeIn(300..399)

    val isClientError: Boolean
        get() = isCodeIn(400..499)

    val isServerError: Boolean
        get() = isCodeIn(500..599)

    val isUnknown: Boolean
        get() = !isCodeIn(100..599)

    private fun isCodeIn(range: IntRange) = code in range
}

class HttpResponse(var status: HttpStatus? = null, var body: String = "") {

    fun jsonPath(s: String): String {
        TODO()
    }
}
