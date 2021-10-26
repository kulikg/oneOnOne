package com.getbridge.homework.boot

import com.getbridge.homework.ACL
import com.getbridge.homework.OneOnOneService
import com.getbridge.homework.UserService
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.lang.System.getenv

internal val dslInitializer = beans {
    val mongoDBConnectionString = getenv("MONGODB_CONNECTION_STRING")
        ?: throw IllegalArgumentException("MONGODB_CONNECTION_STRING env var is missing")
    bean { MongoClients.create(mongoDBConnectionString) }
    bean { ReactiveMongoTemplate(ref(), "oneOnOne") }
    bean<ACL>()
    bean<UserService>()
    bean<OneOnOneService>()
    bean { router(ref(), ref()) }
}