package com.getbridge.homework.boot

import com.getbridge.homework.OneOnOne
import com.getbridge.homework.OneOnOneService
import com.getbridge.homework.User
import com.getbridge.homework.UserService
import org.springframework.http.MediaType.ALL
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.context.Context
import java.util.function.Function
import java.util.function.Supplier

private fun authHeader(context: Context, request: ServerRequest) =
    context.put("user", request.headers().firstHeader("X-AUTHENTICATED-USER").orEmpty())

private fun <T, R> bodyToResponse(request: ServerRequest, bodyType: Class<T>, handler: Function<T, Mono<R>>) =
    request.bodyToMono(bodyType)
        .flatMap(handler)
        .contextWrite { authHeader(it, request) }
        .flatMap { ok().body(fromValue(it!!)) }

private fun <R> pathToResponse(request: ServerRequest, path: String, handler: Function<String, Mono<R>>) =
    request.pathVariable(path)
        .toMono()
        .flatMap(handler)
        .contextWrite { authHeader(it, request) }
        .flatMap { ok().body(fromValue(it!!)) }

private fun <T> response(request: ServerRequest, handler: Supplier<Flux<T>>, responseClass: Class<T>) =
    handler.get()
        .contextWrite { authHeader(it, request) }
        .transform { ok().body(it, responseClass) }
        .single()

internal fun router(oneOnOneService: OneOnOneService, userService: UserService) = router {
    accept(ALL).nest {
        "/users".nest {
            PUT("/add") { bodyToResponse(it, User::class.java, userService::add) }
            GET("/all") { response(it, userService::findAll, User::class.java) }
            DELETE("/delete/{name}") { pathToResponse(it, "name", userService::delete) }
        }
        "/1on1".nest {
            POST("/add") { bodyToResponse(it, OneOnOne::class.java, oneOnOneService::add) }
            PUT("/update") { bodyToResponse(it, OneOnOne::class.java, oneOnOneService::update) }
            GET("/all") { response(it, oneOnOneService::findAll, OneOnOne::class.java) }
            GET("/find/{id}") { pathToResponse(it, "id", oneOnOneService::find) }
            GET("/open") { response(it, oneOnOneService::findOpen, OneOnOne::class.java) }
            GET("/closed") { response(it, oneOnOneService::findClosed, OneOnOne::class.java) }
            DELETE("/delete/{id}") { pathToResponse(it, "id", oneOnOneService::delete) }
        }
    }
}