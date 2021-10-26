package com.getbridge.homework

import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.body
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network.newNetwork
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.core.scheduler.Schedulers.immediate
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class OneOnOneContainer :
    GenericContainer<OneOnOneContainer>(DockerImageName.parse("homework:latest"))

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTest {

    private var user = "organizer"

    companion object {
        private val logger = getLogger(this::class.java)
        private val network = newNetwork()
        private val dataBase = MongoDBContainer(DockerImageName.parse("mongo"))
            .withNetwork(network)
            .withNetworkAliases("mongodb")

        private val underTest = OneOnOneContainer()
            .withNetwork(network)
            .withExposedPorts(8080)
            .withEnv("MONGODB_CONNECTION_STRING", "mongodb://mongodb")
            .dependsOn(dataBase)
            .waitingFor(Wait.forLogMessage(".*Started MainKt.*", 1))
            .withLogConsumer(Slf4jLogConsumer(logger))

        init {
            underTest.start()
        }

    }

    fun asUser(userName: String, runnable: Runnable) {
        user = userName
        runnable.run()
        user = "organizer"
    }

    fun webClient() = WebClient.builder()
        .baseUrl("http://${underTest.host}:${underTest.getMappedPort(8080)}")
        .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .defaultHeader("X-AUTHENTICATED-USER", user)
        .build()

    fun <S> RequestHeadersSpec<*>.expectType(type: Class<S>) = this.exchangeToFlux {
        if (it.statusCode().is2xxSuccessful) {
            it.bodyToFlux(type)
        } else {
            it.bodyToMono(type)
                .doOnNext(::println)
                .subscribeOn(immediate())
                .subscribe()
            Flux.error(Throwable(it.statusCode().toString()))
        }
    }.test()

    fun get(endpoint: String): RequestHeadersSpec<*> = webClient().get().uri(endpoint)
    private fun delete(endpoint: String) = webClient().delete().uri(endpoint)

    fun User.put() = webClient()
        .put()
        .uri("/users/add")
        .body(this.toMono())

    fun OneOnOne.put() = webClient()
        .put()
        .uri("/1on1/update")
        .body(this.toMono())

    fun OneOnOne.post() = webClient()
        .post()
        .uri("/1on1/add")
        .body(this.toMono())

    fun User.delete(): RequestHeadersSpec<*> = delete("/users/delete/${this.name}")
    fun OneOnOne.delete(): RequestHeadersSpec<*> = delete("/1on1/delete/${this.id}")
}