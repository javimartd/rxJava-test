package com.javimartd.test.rx

import android.util.Log
import com.javimartd.test.api.People
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction

object Observables {

    private const val SINGLE_OBSERVABLE = "Single"
    private const val MAYBE_OBSERVABLE = "Maybe"
    private const val COMPLETABLE_OBSERVABLE = "Completable"
    private const val FLOWABLE_OBSERVABLE = "Flowable"

    /**
     * - Observable can emit multiple items
     * - Single, Maybe and Completable are one or no emission of items.
     */

    /**
     * Single is an Observable that always emit only one value or throws an error.
     * Method onNext() and onComplete() of Observable has been combined to onSuccess()
     *
     * Example: A typical use case of Single observable would be when we make a network call
     * in Android and receive a response.
     *
     * We use a Single Observable and a Single Observer.
     */
    fun singleObservable() {
        val observable = Single.create(object: SingleOnSubscribe<People> {
            override fun subscribe(emitter: SingleEmitter<People>) {
                val person = People("Sofia", "Female")
                emitter.onSuccess(person)
            }
        })

        observable
            .subscribe(object: SingleObserver<People> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(SINGLE_OBSERVABLE, "onSubscribe")
                }
                override fun onSuccess(t: People) {
                    Log.i(SINGLE_OBSERVABLE, t.toString())
                }
                override fun onError(e: Throwable) {
                    Log.i(SINGLE_OBSERVABLE, e.toString())
                }
            })
    }

    /**
     * Maybe is an Observable that may or may not emit a value. Is similar to Single only
     * difference being that it allows for no emissions as well.
     *
     * Example: we would like to know if a particular user exists in our db.
     * The user may or may not exist.
     *
     * We use a Maybe Observable and a Maybe Observer.
     */
    fun maybeObservable() {
        val observable = Maybe.create(object: MaybeOnSubscribe<People> {
            override fun subscribe(emitter: MaybeEmitter<People>) {
                val person = People("Sofia", "Female")
                emitter.onSuccess(person)
            }
        })
        observable.
            subscribe(object : MaybeObserver<People> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(MAYBE_OBSERVABLE, "onSubscribe")
                }
                override fun onSuccess(t: People) {
                    Log.i(MAYBE_OBSERVABLE, t.toString())
                }
                override fun onComplete() {
                    Log.i(MAYBE_OBSERVABLE, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(MAYBE_OBSERVABLE, e.toString())
                }
            })
    }

    /**
     * Completable doesn't emit any data, but rather is focused on the status of execution,
     * whether successful or failure.
     *
     * Since no data is emitted in Completable, there is no onNext() or onSuccess().
     *
     * Example: this scenario can be used in cases where PUT API is called and we need to
     * update an existing object to the backend.
     */
    fun completableObservable() {
        val observable = Completable.create(object : CompletableOnSubscribe {
            override fun subscribe(emitter: CompletableEmitter) {
                Thread.sleep(1000)
                emitter.onComplete()
            }
        })

        observable.
            subscribe(object: CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    Log.i(COMPLETABLE_OBSERVABLE, "onSubscribe")
                }
                override fun onComplete() {
                    Log.i(COMPLETABLE_OBSERVABLE, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(COMPLETABLE_OBSERVABLE, e.toString())
                }
            })
    }

    /**
     * Flowable is typically used when an Observable is emitting huge amounts of data but the
     * Observer is not able to handle this data emission. This is known as Back Pressure.
     */
    fun flowableObservable() {
        /*
        0 + 1 = 1
        1 + 2 = 3
        3 + 3 = 6
        6 + 4 = 10
        10 + 5 = 15
        15 + 6 = 21
        21 + 7 = 28
        28 + 8 = 36
        36 + 9 = 45
        45 + 10 = 55
         */
        val observable = Flowable.range(1, 10)
            .reduce(object : BiFunction<Int, Int, Int> {
                override fun apply(t1: Int, t2: Int): Int {
                    return t1 + t2 // 55
                }
            })

        observable
            .subscribe(object : MaybeObserver<Int>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(FLOWABLE_OBSERVABLE, "onSubscribe")
                }
                override fun onSuccess(t: Int) {
                    Log.i(FLOWABLE_OBSERVABLE, t.toString())
                }
                override fun onComplete() {
                    Log.i(FLOWABLE_OBSERVABLE, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(FLOWABLE_OBSERVABLE, e.toString())
                }
            })

        // seed: the initial accumulator value
        // It uses the reduce() operator to add the sum of the integers and emit the final sum value.
        val observableWithSeed = Flowable.range(1, 10)
            .reduce(2, object : BiFunction<Int, Int, Int>{
                override fun apply(t1: Int, t2: Int): Int {
                    return t1 + t2 // 57
                }
            })

        observableWithSeed
            .subscribe(object : SingleObserver<Int>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(FLOWABLE_OBSERVABLE, "onSubscribe")
                }
                override fun onSuccess(t: Int) {
                    Log.i(FLOWABLE_OBSERVABLE, t.toString())
                }
                override fun onError(e: Throwable) {
                    Log.i(FLOWABLE_OBSERVABLE, e.toString())
                }
            })
    }
}