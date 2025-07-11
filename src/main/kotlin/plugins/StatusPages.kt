package com.techbros.plugins

import com.techbros.exceptions.ApiException
import com.techbros.models.responses.ErrorDetail
import com.techbros.models.responses.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                cause.statusCode,
                ErrorResponse(
                    error = ErrorDetail(
                        code = cause.errorCode ?: cause.statusCode.value.toString(),
                        message = cause.message
                    )
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = ErrorDetail(
                        code = "INTERNAL_ERROR",
                        message = "An unexpected error occurred"
                    )
                )
            )
        }
    }
}