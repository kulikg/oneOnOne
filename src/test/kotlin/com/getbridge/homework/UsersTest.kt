package com.getbridge.homework

import org.junit.jupiter.api.Test

class UsersTest : AbstractTest() {

    val jancsi = User("jancsi")
    val juliska = User("juliska")

    @Test
    fun `put and delete users`() {
        jancsi
            .put()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
        juliska
            .put()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
        juliska
            .put()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
        get("/users/all")
            .expectType(User::class.java)
            .expectNextCount(2)
            .verifyComplete()
        juliska
            .delete()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
        get("/users/all")
            .expectType(User::class.java)
            .expectNext(jancsi)
            .verifyComplete()
        jancsi
            .delete()
            .expectType(String::class.java)
            .expectNext("ok")
            .verifyComplete()
    }

}
