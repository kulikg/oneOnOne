package com.getbridge.homework

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import reactor.core.publisher.Mono.deferContextual
import reactor.kotlin.core.publisher.toMono

class ACL {
    private val admin = User("admin")

    fun canRead() = isAdmin()
        .switchIfEmpty(user().map {
            Criteria().orOperator(where("organizer").`is`(it), where("attendee").`is`(it))
        })

    fun canRead(oneOnOne: OneOnOne) = user()
        .filter { it == admin || oneOnOne.organizer == it || oneOnOne.attendee == it }
        .map { oneOnOne }

    fun canDelete(id: String) = isAdmin()
        .map { where("id").`is`(id) }
        .switchIfEmpty(user().map {
            Criteria().andOperator(where("id").`is`(id), where("organizer").`is`(it))
        })

    fun canSave(oneOnOne: OneOnOne) = user()
        .filter { it == admin || oneOnOne.organizer == it }


    private fun isAdmin() = user().filter { it == admin }.map { Criteria() }
    private fun user() = deferContextual { User(it.get("user")).toMono() }
}