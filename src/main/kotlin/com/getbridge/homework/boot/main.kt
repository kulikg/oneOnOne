package com.getbridge.homework.boot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OneOnOneApp

fun main(vararg args: String) {
    runApplication<OneOnOneApp>(*args) {
        addInitializers(dslInitializer)
    }
}
