package com.getbridge.homework

import org.springframework.data.annotation.Id
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.LocalDate

data class User(@Id val name: String)

data class OneOnOne(
    @Id val id: String?,
    val title: String,
    val organizer: User,
    val attendee: User,
    val due: LocalDate,
    val location: String,
    val open: Boolean
)

fun <T> badRequest(message: String) = Mono.error<T>(ResponseStatusException(BAD_REQUEST, message))
fun <T> unauthorized(message: String) = Mono.error<T>(ResponseStatusException(UNAUTHORIZED, message))
