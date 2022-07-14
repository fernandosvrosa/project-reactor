package br.com.reacotor

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 *
 *
 * Publisher <- (subscrebe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with  the subscription) -> Subscriber
 * Subscription <- (request N) subscriber
 * Publisher -> (OnNext) subscriber
 * Util:
 * 1. Publisher sends all the objects requested.
 * 2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> subscriber and subscription will be canceled
 *
 */
class MonoTest {

    companion object {
        private val log = LoggerFactory.getLogger(MonoTest::class.java)
    }

    @Test
    fun `mono subscriber test`() {
        val name = "Fernando da Silva Rosa"
        val mono = Mono.just(name).log()

        mono.subscribe()
        log.info("------------------------------------------------")
        StepVerifier.create(mono).expectNext("Fernando da Silva Rosa").verifyComplete()
    }


    @Test
    fun `mono subscriber consumer test`() {
        val name = "Fernando da Silva Rosa"
        val mono = Mono.just(name).log()

        mono.subscribe { log.info("Value {}", it) }

        log.info("------------------------------------------------")
        StepVerifier.create(mono).expectNext(name).verifyComplete()
    }


    @Test
    fun `mono subscriber consumer error test`() {
        val name = "Fernando da Silva Rosa"
        val mono = Mono.just(name).map {
            throw RuntimeException("Testing mono with error")
        }

        mono.subscribe({ log.info("Name {}", it) }, { log.error("Something bad happened") })

        mono.subscribe({ log.info("Name {}", it) }, { it.printStackTrace() })


        log.info("------------------------------------------------")
        StepVerifier.create(mono).expectError(RuntimeException::class.java).verify()
    }


    @Test
    fun `mono subscriber consumer complete test`() {
        val name = "Fernando da Silva Rosa"
        val mono = Mono.just(name)
            .log()
            .map { it.uppercase() }

        mono.subscribe({ log.info("Value {}", it) }, { it.printStackTrace() }, { log.info("FINISHED!") })

        log.info("------------------------------------------------")
        StepVerifier.create(mono).expectNext(name.uppercase()).verifyComplete()
    }


    @Test
    fun `mono subscriber consumer Subscription test`() {
        val name = "Fernando da Silva Rosa"
        val mono = Mono.just(name)
            .log()
            .map { it.uppercase() }

        mono.subscribe(
            { log.info("Value {}", it) },
            { it.printStackTrace() },
            { log.info("FINISHED!") },
            { it.request(5) })

        log.info("------------------------------------------------")
    }


    @Test
    fun `mono do on methods test`() {
        val name = "Fernando da Silva Rosa"
        val mono = Mono.just(name)
            .log()
            .map { it.uppercase() }
            .doOnSubscribe { log.info("Subscribed") }
            .doOnRequest { log.info("request received, stating doing something...") }
            .doOnNext { log.info("value is here. Executing do onNext {}", it) }
            .flatMap { Mono.empty<Unit>() }
            .doOnNext { log.info("value is here. Executing do onNext {}", it) }
            .doOnSuccess { log.info("doOnSuccess executed {}", it) }

        mono.subscribe({ log.info("Value {}", it) }, { it.printStackTrace() }, { log.info("FINISHED!") })

        log.info("------------------------------------------------")
        //  StepVerifier.create(mono).expectNext(name).verifyComplete()
    }


    @Test
    fun `mono do on error test`() {
        val name = "Fernando da Silva Rosa"
        val error = Mono.error<Any>(IllegalArgumentException("Illegal argument exeception"))
            .doOnError { log.error("Error message: {}", it.message) }
            .onErrorResume {
                log.info("Inside on error resume")
                return@onErrorResume Mono.just(name)
            }
            .log()

        StepVerifier.create(error)
            .expectNext(name)
            .verifyComplete()
    }


    @Test
    fun `mono do on error return test`() {
        val name = "Fernando da Silva Rosa"
        val error = Mono.error<Any>(IllegalArgumentException("Illegal argument exeception"))
            .onErrorReturn("EMPTY")
            .onErrorResume {
                log.info("Inside on error resume")
                return@onErrorResume Mono.just(name)
            }
            .doOnError { log.error("Error message: {}", it.message) }
            .log()

        StepVerifier.create(error)
            .expectNext("EMPTY")
            .verifyComplete()
    }

}