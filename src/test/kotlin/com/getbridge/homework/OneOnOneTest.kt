package com.getbridge.homework

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import reactor.kotlin.test.test
import java.time.LocalDate.now

class OneOnOneTest : AbstractTest() {

    private val attendee = User("attendee")
    private val organizer = User("organizer")
    private val opened = OneOnOne(
        title = "should be open",
        organizer = organizer,
        attendee = attendee,
        due = now(),
        location = "here",
        id = null,
        open = true
    )
    private val toBeClosed = OneOnOne(
        title = "will be closed",
        organizer = organizer,
        attendee = attendee,
        due = now(),
        location = "here",
        id = null,
        open = true
    )
    private val closed = OneOnOne(
        title = "should be closed",
        organizer = organizer,
        attendee = attendee,
        due = now(),
        location = "here",
        id = null,
        open = false
    )

    @BeforeAll
    fun beforeAll() {
        attendee.put()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
        organizer
            .put()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
    }

    @AfterAll
    fun afterAll() {
        attendee.delete()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
        organizer.delete()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
    }

    @AfterEach
    fun afterEach() {
        asUser("admin") {
            webClient().get().uri("/1on1/all")
                .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
                .map {
                    it.delete()
                        .expectType(String::class.java)
                        .expectNextCount(1)
                        .verifyComplete()
                }
                .collectList()
                .test()
                .expectNextCount(1)
                .verifyComplete()
        }
    }

    @Test
    fun `can find own 1on1 by id`() {
        webClient().get().uri("/1on1/find/invalid_id")
            .exchangeToMono { response -> response.bodyToMono(OneOnOne::class.java) }
            .test()
            .verifyError()
        opened.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        webClient().get().uri("/1on1/all")
            .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
            .map {
                webClient().get().uri("/1on1/find/${it.id}")
                    .exchangeToMono { response -> response.bodyToMono(OneOnOne::class.java) }
                    .test()
                    .expectNext(it)
                    .verifyComplete()
                asUser("attendee") {
                    webClient().get().uri("/1on1/find/${it.id}")
                        .exchangeToMono { response -> response.bodyToMono(OneOnOne::class.java) }
                        .test()
                        .expectNext(it)
                        .verifyComplete()
                }
                asUser("not the owner") {
                    webClient().get().uri("/1on1/find/${it.id}")
                        .exchangeToMono { response -> response.bodyToMono(OneOnOne::class.java) }
                        .test()
                        .verifyError()
                }
            }
            .test()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `can update 1on1s`() {
        opened.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        webClient().get().uri("/1on1/all")
            .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
            .map {
                it.copy(title = "title is changed")
                    .put()
                    .expectType(String::class.java)
                    .expectNext("ok")
                    .verifyComplete()
            }
            .test()
            .expectNextCount(1)
            .verifyComplete()
        get("/1on1/all")
            .expectType(OneOnOne::class.java)
            .expectNextMatches { it.title == "title is changed" }
            .verifyComplete()
    }

    @Test
    fun `can't delete non existing 1on1`() {
        opened.copy(id = "non_existing_id").delete()
            .expectType(String::class.java)
            .verifyError()
    }

    @Test
    fun `can't update others 1on1`() {
        opened.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        webClient().get().uri("/1on1/all")
            .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
            .map {
                asUser("I'm not the owner") {
                    it.copy(title = "title is updated")
                        .put()
                        .expectType(String::class.java)
                        .verifyError()
                }
            }
            .test()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `can't delete others 1on1`() {
        opened.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        webClient().get().uri("/1on1/all")
            .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
            .map {
                asUser("I'm not the owner") {
                    it.delete()
                        .expectType(String::class.java)
                        .verifyError()
                }
            }.test()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `can't update closed 1on1s`() {
        closed.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        webClient().get().uri("/1on1/all")
            .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
            .map {
                it.copy(title = "try to change title").put()
                    .expectType(String::class.java)
                    .verifyError()
            }
            .test()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `can't update 1on1s without valid id`() {
        opened.put()
            .expectType(String::class.java)
            .verifyError()
        opened.copy(id = "non_existing_id").put()
            .expectType(String::class.java)
            .verifyError()
    }

    @Test
    fun `1on1s must refer to existing users`() {
        opened.copy(organizer = User("unknown user"))
            .post()
            .expectType(String::class.java)
            .verifyError()
        opened.copy(attendee = User("unknown user"))
            .post()
            .expectType(String::class.java)
            .verifyError()
        opened.copy(organizer = User("unknown user"), attendee = User("unknown user"))
            .post()
            .expectType(String::class.java)
            .verifyError()
    }

    @Test
    fun `possible to close 1on1s and filter them`() {
        toBeClosed.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        webClient().get().uri("/1on1/all")
            .exchangeToFlux { it.bodyToFlux(OneOnOne::class.java) }
            .map {
                it.copy(open = false)
                    .put()
                    .expectType(String::class.java)
                    .expectNext("ok")
                    .verifyComplete()
            }
            .test()
            .expectNextCount(1)
            .verifyComplete()
        opened.post()
            .expectType(String::class.java)
            .expectNextCount(1)
            .verifyComplete()
        get("/1on1/open")
            .expectType(OneOnOne::class.java)
            .expectNextMatches { it.open }
            .verifyComplete()
        get("/1on1/closed")
            .expectType(OneOnOne::class.java)
            .expectNextMatches { !it.open }
            .verifyComplete()
    }
}