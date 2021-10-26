package com.getbridge.homework

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono


class OneOnOneService(
    private val repository: ReactiveMongoTemplate,
    private val userService: UserService,
    private val acl: ACL
) {

    fun findAll() = acl.canRead()
        .flatMapMany { repository.find(Query(it), OneOnOne::class.java) }

    fun find(id: String) = repository.findById(id, OneOnOne::class.java)
        .switchIfEmpty { badRequest("nothing found") }
        .flatMap(acl::canRead)
        .switchIfEmpty { unauthorized("you don't have permissions") }

    fun delete(id: String) = acl.canDelete(id)
        .flatMap { repository.findAndRemove(Query(it), OneOnOne::class.java) }
        .switchIfEmpty { badRequest<OneOnOne>("did not delete anything") }

    fun findOpen() = checkOpen(true)
        .flatMapMany { repository.find(Query(it), OneOnOne::class.java) }

    fun findClosed() = checkOpen(false)
        .flatMapMany { repository.find(Query(it), OneOnOne::class.java) }

    fun update(oneOnOne: OneOnOne) = oneOnOne
        .toMono()
        .filter { it.id != null }
        .switchIfEmpty { badRequest("id is null in given 1on1 data") }
        .flatMap { repository.findById(it.id!!, OneOnOne::class.java) }
        .switchIfEmpty { badRequest("1on1 event with id ${oneOnOne.id} is not found") }
        .filter(OneOnOne::open)
        .switchIfEmpty { badRequest("1on1 event with id ${oneOnOne.id} is already closed") }
        .flatMap { add(oneOnOne) }

    fun add(oneOnOne: OneOnOne) = userService.filterExistingUsers(oneOnOne.organizer, oneOnOne.attendee)
        .flatMap { badRequest<String>("unknown users: $it") }
        .switchIfEmpty {
            acl.canSave(oneOnOne)
                .switchIfEmpty { unauthorized("you don't have permission to save 1on1") }
                .flatMap { repository.save(oneOnOne).map(OneOnOne::id) }
        }

    private fun checkOpen(isOpen: Boolean) = acl.canRead()
        .map { Criteria().andOperator(where("open").`is`(isOpen), it) }
}
