package com.getbridge.homework

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

class UserService(private val repository: ReactiveMongoTemplate) {
    fun add(user: User) = repository.save(user)
        .map { "ok" }

    fun findAll() = repository.findAll(User::class.java)

    fun delete(userName: String) = where("name").`is`(userName)
        .toMono()
        .flatMap { repository.remove(Query(it), User::class.java) }
        .map { "ok" }

    fun filterExistingUsers(vararg users: User) = users.toFlux()
        .parallel()
        .runOn(Schedulers.parallel())
        .flatMap(::yieldWhenUserNotExist)
        .sequential()
        .collectList()
        .filter { it.isNotEmpty() }

    private fun yieldWhenUserNotExist(user: User) = where("name").`is`(user.name)
        .toMono()
        .flatMapMany { repository.exists(Query(it), User::class.java) }
        .filter { !it }
        .map { user }
}