package com.javimartd.test.rx

import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.*

object Subjects {

    private const val FIRST_OBSERVER_ON_NEXT = "Observer 1 onNext: "
    private const val SECOND_OBSERVER_ON_NEXT = "Observer 2 onNext: "
    private const val PUBLISH_SUBJECT = "publish subject"
    private const val BEHAVIOR_SUBJECT = "behavior subject"
    private const val REPLAY_SUBJECT = "replay subject"
    private const val ASYNC_SUBJECT = "async subject"
    private const val UNICAST_SUBJECT = "unicast subject"

    fun usoOfSubject() {
        val observable = Observable
            .range(1, 5)
            .subscribeOn(Schedulers.io())
            .map { item -> run {
                Log.i("Squaring with itself", item.toString())
                item * item
            }}

        val replaySubject = ReplaySubject.create<Int>()

        observable.subscribe(replaySubject)

        /*
          You can see from the output that the map() operation only takes place once, even if there are 2 subscribers.
          So if we have 10 subscribers, the map() operation will take place only once.
         */
        replaySubject.subscribe{item -> Log.i("Subject one: ", item.toString())}
        replaySubject.subscribe{item -> Log.i("Subject two: ", item.toString())}
        replaySubject.subscribe{item -> Log.i("Subject three: ", item.toString())}
    }

    /**
     * PublishSubject emits all the items at the point of subscription.
     * This is the most basic form of Subject.
     */
    fun publishSubject() {
        val publishSubject = PublishSubject.create<Int>()

        /*publishSubject.subscribe(object : Observer<Int>{
            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {}
            override fun onNext(t: Int) {}
            override fun onError(e: Throwable) {}
        })*/

        publishSubject.onNext(0)

        /*publishSubject.subscribe(
            { Log.i(FIRST_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(PUBLISH_SUBJECT, it.toString()) },
            { Log.i(PUBLISH_SUBJECT, "onComplete") },
            { Log.i(PUBLISH_SUBJECT, "onSubscribe") }
        )*/

        publishSubject.onNext(1)
        publishSubject.onNext(2)

        /*publishSubject.subscribe(
            { Log.i(SECOND_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(PUBLISH_SUBJECT + "2", it.toString()) },
            { Log.i(PUBLISH_SUBJECT + "2", "onComplete") },
            { Log.i(PUBLISH_SUBJECT + "2", "onSubscribe") }
        )*/

        publishSubject.onNext(3)
        publishSubject.onNext(4)
    }

    /**
     * BehaviorSubject prints the most recently emitted value before the subscription
     * and all the values after the subscription.
     *
     * Difference between PublishSubject and BehaviorSubject is that PublishSubject prints all values
     * after subscription and BehaviorSubject prints the last emitted value before subscription and all
     * the values after subscription.
     */
    private fun behaviorSubject() {
        /*
          Difference between PublishSubject and BehaviorSubject is that PublishSubject prints all values after
          subscription and BehaviorSubject prints the last emitted value before subscription and all the
          values after subscription.
         */
        val behaviorSubject = BehaviorSubject.create<Int>()
        behaviorSubject.onNext(0)
        /*behaviorSubject.subscribe(
            { Log.i(FIRST_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(BEHAVIOR_SUBJECT, it.toString()) },
            { Log.i(BEHAVIOR_SUBJECT, "onComplete") },
            { Log.i(BEHAVIOR_SUBJECT, "onSubscribe") }
        )*/
        behaviorSubject.onNext(1)
        behaviorSubject.onNext(2)
        /*behaviorSubject.subscribe(
            { Log.i(SECOND_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(BEHAVIOR_SUBJECT + "2", it.toString()) },
            { Log.i(BEHAVIOR_SUBJECT + "2", "onComplete") },
            { Log.i(BEHAVIOR_SUBJECT + "2", "onSubscribe") }
        )*/
        behaviorSubject.onNext(3)
        behaviorSubject.onNext(4)
    }

    /**
     * ReplaySubject emits all the items of the Observable, regardless of when the subscriber subscribes.
     */
    fun replaySubject() {
        val replaySubject = ReplaySubject.create<Int>()
        replaySubject.onNext(0)
        /*replaySubject.subscribe(
            { Log.i(FIRST_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(REPLAY_SUBJECT, it.toString()) },
            { Log.i(REPLAY_SUBJECT, "onComplete") },
            { Log.i(REPLAY_SUBJECT, "onSubscribe") }
        )*/
        replaySubject.onNext(1)
        replaySubject.onNext(2)
        /*replaySubject.subscribe(
            { Log.i(SECOND_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(REPLAY_SUBJECT + "2", it.toString()) },
            { Log.i(REPLAY_SUBJECT + "2", "onComplete") },
            { Log.i(REPLAY_SUBJECT + "2", "onSubscribe") }
        )*/
        replaySubject.onNext(3)
        replaySubject.onNext(4)
    }

    /**
     * AsyncSubject emits only the last value of the Observable and this only happens after the Observable completes.
     */
    fun asyncSubject() {
        val asyncSubject = AsyncSubject.create<Int>()
        asyncSubject.onNext(0)
        /*asyncSubject.subscribe(
            { Log.i(FIRST_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(ASYNC_SUBJECT, it.toString()) },
            { Log.i(ASYNC_SUBJECT, "onComplete") },
            { Log.i(ASYNC_SUBJECT, "onSubscribe") }
        )*/
        asyncSubject.onNext(1)
        asyncSubject.onNext(2)
        /*asyncSubject.subscribe(
            { Log.i(SECOND_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(ASYNC_SUBJECT + "2", it.toString()) },
            { Log.i(ASYNC_SUBJECT + "2", "onComplete") },
            { Log.i(ASYNC_SUBJECT + "2", "onSubscribe") }
        )*/
        asyncSubject.onNext(3)
        asyncSubject.onNext(4)

        /*
         This is very important in AsyncSubject
         Only after onComplete() is called, the last emitted value is printed by both Observers.
         */
        asyncSubject.onComplete()
    }

    /**
     * UnicastSubject allows only a single subscriber and it emits all the items regardless of the time of subscription.
     */
    fun unicastSubject() {
        val unicastSubject = UnicastSubject.create<Int>()
        unicastSubject.onNext(0)
        /*unicastSubject.subscribe(
            { Log.i(FIRST_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(UNICAST_SUBJECT, it.toString()) },
            { Log.i(UNICAST_SUBJECT, "onComplete") },
            { Log.i(UNICAST_SUBJECT, "onSubscribe") }
        )*/
        unicastSubject.onNext(1)
        unicastSubject.onNext(2)
        /*unicastSubject.subscribe(
            { Log.i(SECOND_OBSERVER_ON_NEXT, it.toString()) },
            { Log.i(UNICAST_SUBJECT + "2", it.toString()) },
            { Log.i(UNICAST_SUBJECT + "2", "onComplete") },
            { Log.i(UNICAST_SUBJECT + "2", "onSubscribe") }
        )*/
        unicastSubject.onNext(3)
        unicastSubject.onNext(4)
    }
}