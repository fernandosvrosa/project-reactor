package br.com.reacotor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.nio.file.Files
import java.nio.file.Path
import java.util.Objects
import java.util.concurrent.atomic.AtomicLong

class OperatorsTest {

    companion object {
        private val log = LoggerFactory.getLogger(OperatorsTest::class.java)
    }

    @Test
    fun `subscribe on simple`() {
       val flux = Flux.range(1, 4)
            .map {
                log.info("map 1 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }
            .subscribeOn(Schedulers.boundedElastic())
            .map {
                log.info("map 2 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }


        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext(1,2,3,4)
            .verifyComplete()
    }


    @Test
    fun `publish on simple`() {
        val flux = Flux.range(1, 4)
            .map {
                log.info("map 1 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                log.info("map 2 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }


        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext(1,2,3,4)
            .verifyComplete()
    }


    @Test
    fun `multiple subscribe on simple`() {
        val flux = Flux.range(1, 4)
            .subscribeOn(Schedulers.single())
            .map {
                log.info("map 1 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }
            .subscribeOn(Schedulers.boundedElastic())
            .map {
                log.info("map 2 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }


        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext(1,2,3,4)
            .verifyComplete()
    }


    @Test
    fun `multiple publish on simple`() {
        val flux = Flux.range(1, 4)
            .publishOn(Schedulers.single())
            .map {
                log.info("map 1 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                log.info("map 2 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }


        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext(1,2,3,4)
            .verifyComplete()
    }


    @Test
    fun `publish and subscribe on simple`() {
        val flux = Flux.range(1, 4)
            .publishOn(Schedulers.single())
            .map {
                log.info("map 1 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }
            .subscribeOn(Schedulers.boundedElastic())
            .map {
                log.info("map 2 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }


        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext(1,2,3,4)
            .verifyComplete()
    }


    @Test
    fun `subscribe and publish on simple`() {
        val flux = Flux.range(1, 4)
            .subscribeOn(Schedulers.single())
            .map {
                log.info("map 1 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }
            .publishOn(Schedulers.boundedElastic())
            .map {
                log.info("map 2 - number {} Thread {}", it, Thread.currentThread().name)
                return@map it
            }


        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext(1,2,3,4)
            .verifyComplete()
    }


    @Test
    fun `subscribe on IO`() {
      val list =  Mono.fromCallable { Files.readAllLines(Path.of("text-file")) }
            .log()
            .subscribeOn(Schedulers.boundedElastic())

        //list.subscribe{ log.info("{}", it)}

        StepVerifier.create(list)
            .expectSubscription()
            .thenConsumeWhile {
                Assertions.assertFalse(it.isEmpty())
                log.info("Size {}", it.size)
                return@thenConsumeWhile true
            }
            .verifyComplete()
    }

    @Test
    fun `switch if empty operator test`() {
        val flux = emptyFlux()
            .switchIfEmpty(Flux.just("not empty anymore"))
            .log()

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNext("not empty anymore")
            .expectComplete()
            .verify()
    }

    @Test
    fun `defer operator test`() {
      val defer =  Mono.defer { Mono.just(System.currentTimeMillis())}

        defer.subscribe{ log.info("Time {}", it)}
        Thread.sleep(100)
        defer.subscribe{ log.info("Time {}", it)}
        Thread.sleep(100)
        defer.subscribe{ log.info("Time {}", it)}
        Thread.sleep(100)
        defer.subscribe{ log.info("Time {}", it)}

        val atomicLong = AtomicLong()
        defer.subscribe(atomicLong::set)
        Assertions.assertTrue(atomicLong.get() > 0)
    }


    private fun emptyFlux() = Flux.empty<Any>()




}