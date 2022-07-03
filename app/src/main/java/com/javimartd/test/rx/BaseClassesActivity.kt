package com.javimartd.test.rx

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.javimartd.test.api.People
import com.javimartd.test.api.Planet
import com.javimartd.test.databinding.ActivityBaseClassesBinding
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable

class BaseClassesActivity : AppCompatActivity() {

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
                emitter.onComplete()
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
                Log.i(OBSERVABLE, "onError, ${e.message}")
            }
        })
    }

    /**
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
                Log.i(SINGLE, "onError, ${e.message}")
            }
        })
    }

    /**
     * http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Maybe.html
     */
    private fun maybe() {
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
                Log.i(MAYBE, "onError, ${e.message}")
            }
        })
    }

    /**
     * http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Completable.html
     */
    private fun completable() {
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
                Log.i(COMPLETABLE, "onError, ${e.message}")
            }
        })
    }

    /**
     * http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html
     */
    private fun flowable() {
        val flowable = Flowable.create(object : FlowableOnSubscribe<Int> {
            override fun subscribe(emitter: FlowableEmitter<Int>) {
                emitter.onNext(3)
                Thread.sleep(1000)
                emitter.onComplete()
            }
        }, BackpressureStrategy.BUFFER)

        flowable.subscribe {
            Log.i(FLOWABLE, "onNext, $it")
        }
    }
}