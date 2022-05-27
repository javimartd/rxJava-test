package com.javimartd.test.rx

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.javimartd.test.api.People
import com.javimartd.test.api.Planet
import com.javimartd.test.databinding.ActivityBaseClassesBinding
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction

class BaseClassesActivity : AppCompatActivity() {

    companion object {
        private const val OBSERVABLE = "OBSERVABLE"
        private const val SINGLE = "SINGLE"
        private const val MAYBE = "MAYBE"
        private const val COMPLETABLE = "COMPLETABLE"
        private const val FLOWABLE = "FLOWABLE"
    }

    private lateinit var binding: ActivityBaseClassesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRun.setOnClickListener {
            observable()
        }
    }

    /**
     * https://reactivex.io/documentation/observable.html
     */
    private fun observable() {
        val observable = Observable.create(object: ObservableOnSubscribe<People> {
            override fun subscribe(emitter: ObservableEmitter<People>) {
                val people = People("Beru Whitesun lars", "female")
                emitter.onNext(people)
            }
        })
        observable.subscribe(object: Observer<People>{
            override fun onSubscribe(d: Disposable) {
                Log.i(OBSERVABLE, "onSubscribe")
            }
            override fun onNext(t: People) {
                Log.i(OBSERVABLE, "onNext, $t")
            }
            override fun onComplete() {
                Log.i(OBSERVABLE, "onComplete")
            }
            override fun onError(e: Throwable) {
                Log.i(OBSERVABLE, "onError, " + e.message)
            }
        })
    }

    /**
     * Single emits only one value or throws an error.
     * Methods onNext() and onComplete() have been combined to onSuccess()
     *
     * Example: network call on Android and receive a response.
     *
     * https://reactivex.io/documentation/single.html
     */
    private fun single() {
        val single = Single.create(object: SingleOnSubscribe<Planet> {
            override fun subscribe(emitter: SingleEmitter<Planet>) {
                val person = Planet("Yavin IV", "temperate, tropical")
                emitter.onSuccess(person)
            }
        })
        single.subscribe(object: SingleObserver<Planet> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(SINGLE, "onSubscribe")
                }
                override fun onSuccess(t: Planet) {
                    Log.i(SINGLE, "onSuccess, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(SINGLE, "onError, " + e.message)
                }
            })
    }

    /**
     * Maybe is an Observable that may or may not emit a value. Is similar to Single only
     * difference being that it allows for no emissions as well.
     *
     * Example: we would like to know if a particular user exists in our DB.
     * The user may or may not exist.
     *
     * http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Maybe.html
     */
    fun maybe() {
        val maybe = Maybe.create(object: MaybeOnSubscribe<String> {
            override fun subscribe(emitter: MaybeEmitter<String>) {
                emitter.onSuccess("Hi there")
            }
        })
        maybe.subscribe(object : MaybeObserver<String> {
            override fun onSubscribe(d: Disposable) {
                Log.i(MAYBE, "onSubscribe")
            }
            override fun onSuccess(t: String) {
                Log.i(MAYBE, "onSuccess, $t")
            }
            override fun onComplete() {
                Log.i(MAYBE, "onComplete")
            }
            override fun onError(e: Throwable) {
                Log.i(MAYBE, "onError, " + e.message)
            }
        })
    }

    /**
     * Completable doesn't emit any data, but rather is focused on the status of execution,
     * whether successful or failure. Since no data is emitted in Completable, there is no
     * onNext() or onSuccess().
     *
     * Example: this scenario can be used in cases where PUT API is called and we need to
     * update an existing object to the backend.
     *
     * http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Completable.html
     */
    fun completable() {
        val completable = Completable.create(object : CompletableOnSubscribe {
            override fun subscribe(emitter: CompletableEmitter) {
                Thread.sleep(1000)
                emitter.onComplete()
            }
        })

        completable.subscribe(object: CompletableObserver {
            override fun onSubscribe(d: Disposable) {
                Log.i(COMPLETABLE, "onSubscribe")
            }
            override fun onComplete() {
                Log.i(COMPLETABLE, "onComplete")
            }
            override fun onError(e: Throwable) {
                Log.i(COMPLETABLE, "onError, " + e.message)
            }
        })
    }

    /**
     * Flowable is typically used when an Observable is emitting huge amounts of data but the
     * Observer is not able to handle this data emission. This is known as Back Pressure.
     *
     * http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html
     */
    fun flowable() {
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
        val flowable = Flowable.range(1, 10)
            .reduce(object : BiFunction<Int, Int, Int> {
                override fun apply(t1: Int, t2: Int): Int {
                    return t1 + t2 // 55
                }
            })

        flowable.subscribe(object : MaybeObserver<Int>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(FLOWABLE, "onSubscribe")
                }
                override fun onSuccess(t: Int) {
                    Log.i(FLOWABLE, "onSuccess, $t")
                }
                override fun onComplete() {
                    Log.i(FLOWABLE, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(FLOWABLE, "onError, " + e.message)
                }
            })

        // seed: the initial accumulator value
        // It uses the reduce operator to add the sum of the integers and emit the final sum value.
        val flowableWithSeed = Flowable.range(1, 10)
            .reduce(2, object : BiFunction<Int, Int, Int>{
                override fun apply(t1: Int, t2: Int): Int {
                    return t1 + t2 // 57
                }
            })

        flowableWithSeed.subscribe(object : SingleObserver<Int>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(FLOWABLE, "onSubscribe")
                }
                override fun onSuccess(t: Int) {
                    Log.i(FLOWABLE, "onSuccess, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(FLOWABLE, "onError, " + e.message)
                }
            })
    }
}