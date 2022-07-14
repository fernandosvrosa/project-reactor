package br.com.reacotor

import org.junit.jupiter.api.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Duration

class FluxTest {

    companion object {
        private val log = LoggerFactory.getLogger(FluxTest::class.java)
    }

    @Test
    fun `flux subscriber test`() {
        val flux = Flux.just("Fernando", "Silva", "Rosa")
            .log()


        StepVerifier.create(flux)
            .expectNext("Fernando", "Silva", "Rosa")
            .verifyComplete()
    }

    @Test
    fun `flux subscriber mumbers test`() {
        val flux = Flux.range(1, 5)
            .log()

        flux.subscribe { log.info("Number {}", it) }

        log.info("-------------------------")

        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4, 5)
            .verifyComplete()
    }


    @Test
    fun `flux subscriber from list test`() {
        val flux = Flux.fromIterable(listOf(1, 2, 3, 4, 5))
            .log()

        flux.subscribe { log.info("Number {}", it) }

        log.info("-------------------------")

        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4, 5)
            .verifyComplete()
    }

    @Test
    fun `flux subscriber mumbers error test`() {
        val flux = Flux.range(1, 5)
            .log()
            .map {
                if (it == 4) {
                    throw IndexOutOfBoundsException("index error")
                }
                return@map it
            }

        flux.subscribe({ log.info("Number {}", it) }, { it.printStackTrace() },
            { log.info("DONE!") }, { it.request(3) })

        log.info("-------------------------")

        StepVerifier.create(flux)
            .expectNext(1, 2, 3)
            .expectError(IndexOutOfBoundsException::class.java)
            .verify()
    }


    @Test
    fun `flux subscriber mumbers ugly backpressure test`() {
        val flux = Flux.range(1, 10)
            .log()


        flux.subscribe(object : Subscriber<Int?> {
            private var count = 0
            private var subscription: Subscription? = null
            private val requestCount = 2
            override fun onSubscribe(subscription: Subscription) {
                this.subscription = subscription
                subscription.request(2)
            }

            override fun onNext(integer: Int?) {
                count++
                if (count >= requestCount) {
                    count = 0
                    subscription?.request(2)
                }
            }

            override fun onError(t: Throwable) {}
            override fun onComplete() {}
        })

        log.info("-------------------------")

        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .verifyComplete()
    }


    @Test
    fun `flux subscriber mumbers not so ugly backpressure test`() {
        val flux = Flux.range(1, 10)
            .log()

        flux.subscribe(object : BaseSubscriber<Int>() {
            private var count = 0
            private val requestCount = 2

            override fun hookOnSubscribe(subscription: Subscription) {
                request(requestCount.toLong())
            }

            override fun hookOnNext(value: Int) {
                count++
                if (count >= requestCount) {
                    count = 0
                    request(requestCount.toLong())
                }
            }
        })

        log.info("-------------------------")

        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .verifyComplete()
    }

    @Test
    fun `flux subscriber pretty backpressure test`() {
        val flux = Flux.range(1, 10)
            .log()
            .limitRate(3)

        flux.subscribe { log.info("Number {}", it) }

        log.info("-------------------------")

        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .verifyComplete()
    }


    @Test
    fun `flux subscriber interval one test`() {
        val interval = Flux.interval(Duration.ofMillis(100))
            .take(10)
            .log()

        interval.subscribe { log.info("number {}", it) }

        Thread.sleep(3000)

    }

    @Test
    fun `flux subscriber interval two test`() {
        StepVerifier.withVirtualTime { createInterval() }
            .expectSubscription()
            .expectNoEvent(Duration.ofDays(1))
            .thenAwait(Duration.ofDays(1))
            .expectNext(0L)
            .thenAwait(Duration.ofDays(1))
            .expectNext(1L)
            .thenCancel()
            .verify()
    }

    private fun createInterval() = Flux.interval(Duration.ofDays(1))
        .log()


    @Test
    fun `connectable flux test`() {
        val connectableFlux = Flux.range(1, 10)
            .log()
            .delayElements(Duration.ofMillis(100))
            .publish()

        //      connectableFlux.connect()

//        log.info("Thread sleeping for 300ms")
//        Thread.sleep(300)
//
//        connectableFlux.subscribe{ log.info("sub1 number {}", it)}
//
//        log.info("Thread sleeping for 200ms")
//        Thread.sleep(200)
//
//        connectableFlux.subscribe{ log.info("sub2 number {}", it)}


        StepVerifier.create(connectableFlux)
            .then(connectableFlux::connect)
            .thenConsumeWhile { it <= 5 }
            .expectNext( 6, 7, 8, 9, 10)
            .expectComplete()
            .verify()

    }



    @Test
    fun `connectable flux auto connect test`() {
        val fluxAutoConnect = Flux.range(1, 5)
            .log()
            .delayElements(Duration.ofMillis(100))
            .publish()
            .autoConnect(3)

        StepVerifier.create(fluxAutoConnect)
            .then(fluxAutoConnect::subscribe)
            .then(fluxAutoConnect::subscribe)
            .expectNext( 1,2,3,4,5)
            .expectComplete()
            .verify()

    }


}