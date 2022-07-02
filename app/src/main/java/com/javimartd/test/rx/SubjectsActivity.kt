package com.javimartd.test.rx

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.javimartd.test.databinding.ActivitySubjectsBinding
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.*

/**
 * https://reactivex.io/documentation/subject.html
 */
class SubjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRun.setOnClickListener {
            unicastSubject()
        }
    }

    /**
     * It emits all the items at the point of subscription.
     */
    private fun publishSubject() {

        val publishSubject = PublishSubject.create<Double>()

        publishSubject
            .subscribe(object : Observer<Double> {
                override fun onComplete() {
                    Log.i(PUBLISH_SUBJECT, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.i(PUBLISH_SUBJECT, "onSubscribe")
                }
                override fun onNext(t: Double) {
                    Log.i(PUBLISH_SUBJECT, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(PUBLISH_SUBJECT, "onError, ${e.message}")
                }
            })

        publishSubject.onNext(0.0)
        publishSubject.onNext(1.0)

        publishSubject
            .subscribe(object : Observer<Double> {
                override fun onComplete() {
                    Log.i(PUBLISH_SUBJECT, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.i(PUBLISH_SUBJECT, "onSubscribe")
                }
                override fun onNext(t: Double) {
                    Log.i(PUBLISH_SUBJECT, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(PUBLISH_SUBJECT, "onError, ${e.message}")
                }
            })

        publishSubject.onNext(2.0)
        publishSubject.onNext(3.0)
        publishSubject.onNext(4.0)

        // share exactly the same data stream with all subscriptions
        publishSubject.onNext(Math.random())
    }

    /**
     * It emits the item most recently emitted by the source
     */
    private fun behaviorSubject() {

        val behaviorSubject = BehaviorSubject.create<Int>()

        behaviorSubject.onNext(0)
        behaviorSubject.onNext(1)
        behaviorSubject.onNext(2)

        behaviorSubject.subscribe{
            Log.i(BEHAVIOR_SUBJECT, "onNext, $it")
        }
    }

    /**
     * It emits all the items of the source, regardless of when the subscriber subscribes.
     */
    private fun replaySubject() {

        val replaySubject = ReplaySubject.create<String>()

        replaySubject.onNext("a")
        replaySubject.onNext("b")
        replaySubject.onNext("c")

        replaySubject
            .subscribe(object : Observer<String> {
                override fun onComplete() {
                    Log.i(REPLAY_SUBJECT, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.i(REPLAY_SUBJECT, "onSubscribe")
                }
                override fun onNext(t: String) {
                    Log.i(REPLAY_SUBJECT, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(REPLAY_SUBJECT, "onError, ${e.message}")
                }
            })

        replaySubject.onNext("d")

        replaySubject
            .subscribe(object : Observer<String> {
                override fun onComplete() {
                    Log.i(REPLAY_SUBJECT, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.i(REPLAY_SUBJECT, "onSubscribe")
                }
                override fun onNext(t: String) {
                    Log.i(REPLAY_SUBJECT, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(REPLAY_SUBJECT, "onError, ${e.message}")
                }
            })

        replaySubject.onNext("e")
    }

    /**
     * It emits only the last value of the source and this only happens after the source completes.
     */
    private fun asyncSubject() {

        val asyncSubject = AsyncSubject.create<Int>()

        asyncSubject.onNext(0)

        asyncSubject
            .subscribe(object : Observer<Int> {
                override fun onComplete() {
                    Log.i(ASYNC_SUBJECT, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.i(ASYNC_SUBJECT, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(ASYNC_SUBJECT, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(ASYNC_SUBJECT, "onError, ${e.message}")
                }
            })

        asyncSubject.onNext(1)
        asyncSubject.onNext(2)

        asyncSubject.onComplete()
    }

    /**
     *  It allows only a single subscriber and buffers all emissions it receives until an Observer
     *  subscribes to it, and releases all these emissions to the Observer and clear its cache.
     */
    private fun unicastSubject() {

        val unicastSubject = UnicastSubject.create<Int>()

        unicastSubject.onNext(0)
        unicastSubject.onNext(1)
        unicastSubject.onNext(2)

        unicastSubject
            .subscribe(object : Observer<Int> {
                override fun onComplete() {
                    Log.i(UNICAST_SUBJECT, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.i(UNICAST_SUBJECT, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(UNICAST_SUBJECT, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(UNICAST_SUBJECT, "onError, ${e.message}")
                }
            })

        unicastSubject.onNext(3)
    }
}