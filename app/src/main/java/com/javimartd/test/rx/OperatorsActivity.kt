package com.javimartd.test.rx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.javimartd.test.EmptyActivity
import com.javimartd.test.api.DataSource
import com.javimartd.test.api.People
import com.javimartd.test.api.RemoteDataSource
import com.javimartd.test.databinding.ActivityOperatorsBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class OperatorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperatorsBinding

    private lateinit var disposable: Disposable
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var buttonEmitObservable: Observable<Int>

    private lateinit var remoteDataSource: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperatorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonOperator.setOnClickListener {
            filterOperator()
        }
        binding.buttonCancel.setOnClickListener {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        binding.buttonStartActivity.setOnClickListener {
            startActivity(Intent(this, EmptyActivity::class.java))
        }

        createButtonObservable()

        remoteDataSource = RemoteDataSource()
    }

    override fun onResume() {
        super.onResume()
        Log.i(OperatorsActivity::class.java.name, "onResume")
    }
    override fun onPause() {
        super.onPause()
        Log.i(OperatorsActivity::class.java.name, "onPause")
    }
    override fun onStop() {
        super.onStop()
        Log.i(OperatorsActivity::class.java.name, "onStop")
    }
    override fun onDestroy() {
        super.onDestroy()
        //compositeDisposable.clear()
        // A memory leak like this can be caused by observables which retain a copy of the
        // Android context.
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    private fun createButtonObservable() {
        var counter = 0
        disposable = Observable.create<Int> { emitter ->
            binding.buttonEmit.setOnClickListener {
                emitter.onNext(counter)
                counter++
            }
            emitter.setCancellable {
                // setCancelable method will fire when the observable is unsubscribed.
                binding.buttonEmit.setOnClickListener(null)
            }
        }.subscribe(object : Consumer<Int> {
            // This interface is handy when you want to set up a simple subscription to an Observable.
            override fun accept(t: Int) {
                setMessage(t.toString())
            }
        })
    }

    //region CREATING OBSERVABLES
    /**
     * https://reactivex.io/documentation/operators/create.html
     */
    private fun createOperator() {
        val alphabets = listOf("a", "b", "c", "d", "e", "f")
        val observable = Observable.create(object : ObservableOnSubscribe<String> {
            override fun subscribe(emitter: ObservableEmitter<String>) {
                try {
                    alphabets.forEach {
                        emitter.onNext(it)
                    }
                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        })
        observable
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(CREATE_OPERATOR, "onSubscribe")
                }
                override fun onNext(s: String) {
                    Log.i(CREATE_OPERATOR, "onNext, $s")
                }
                override fun onComplete() {
                    Log.i(CREATE_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(CREATE_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/just.html
     */
    private fun justOperator() {
        Observable.just(1,2,3,4, 5)
            .subscribe(object: Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(JUST_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(JUST_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(JUST_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(JUST_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/from.html
     */
    private fun fromOperator() {
        Observable.fromArray("A", "B", "C", "D", "E", "F")
            .subscribe(object : Observer<String>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(FROM_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: String) {
                    Log.i(FROM_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(FROM_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(FROM_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     *
     */
    private fun fromCallableOperator() {
        disposable = Observable.fromCallable {
            // do something and return
            return@fromCallable "Hi!"
        }
            .subscribeOn(Schedulers.io())
            .subscribe {
                Log.d(FROM_CALLABLE_OPERATOR, it)
            }
    }

    /**
     * https://reactivex.io/documentation/operators/timer.html
     */
    private fun timerOperator() {
        Observable.timer(3, TimeUnit.SECONDS)
            .subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(TIMER_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Long) {
                    Log.i(TIMER_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(TIMER_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(TIMER_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/interval.html
     */
    private fun intervalOperator() {
        Observable.interval(1, TimeUnit.SECONDS)
            .take(10)
            .subscribe(object: Observer<Long>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(INTERVAL_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Long) {
                    Log.i(INTERVAL_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(INTERVAL_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(INTERVAL_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/range.html
     */
    private fun rangeOperator() {
        Observable.range(2,10)
            .subscribe(object: Observer<Int>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(RANGE_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(RANGE_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(RANGE_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(RANGE_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/repeat.html
     */
    private fun repeatOperator() {
        Observable.just(1,2, 3)
            .repeat(3)
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(REPEAT_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(REPEAT_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(REPEAT_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(REPEAT_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     *
     * repeat operator resubscribes when it receives onCompleted()
     * retry operator resubscribes when it receives onError().
     */
    private fun repeatWhenOperator() {
        remoteDataSource.getPeople("4")
            .repeatWhen { completed -> completed.delay(3, TimeUnit.SECONDS) }
            .doOnComplete { Log.i(REPEAT_WHEN_OPERATOR, "doOnComplete") }
            .doOnNext { Log.i(REPEAT_WHEN_OPERATOR, "doOnNext") }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<People>{
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                    Log.i(REPEAT_WHEN_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: People) {
                    Log.i(REPEAT_WHEN_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(REPEAT_WHEN_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(REPEAT_WHEN_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     *
     */
    private fun repeatUntilOperator() {

    }

    /**
     * This operator does not create the Observable until the Observer subscribes.
     * The only downside to defer() is that it creates a new Observable each time you
     * get a new Observer.
     */
    private fun deferOperator() {

    }

    //endregion

    //region TRANSFORMING OBSERVABLES
    /**
     * https://reactivex.io/documentation/operators/map.html
     */
    private fun mapOperator() {
        disposable = Observable.just(1,2,3)
            .map { number -> number * number }
            .subscribe {
                Log.i(MAP_OPERATOR, "onNext, $it")
            }
    }

    /**
     *
     */
    private fun groupByOperator() {
        val observable = Observable.just(
            People("Luke", "male"),
            People ("C130", "N/A"),
            People ("Pade", "female")
        )
        disposable = observable
            .groupBy {
                it.gender == "male"
            }
            .subscribe {
                Log.i(GROUP_BY_OPERATOR, "")
            }
    }

    /**
     * The flatMap operator help you to transform one event to another Observable
     * (or transform an event to zero, one, or more events). It's a perfect operator when
     * you want to call another method which return an Observable.
     */
    private fun flatMapOperator() {
        var names = ""
        disposable = remoteDataSource.getPeople("1")
            .map {
                firstResponse -> names = "first response: " + firstResponse.name
                Log.i(FLAT_MAP_OPERATOR, "first response")
            }
            .flatMap {
                remoteDataSource.getPeople("2")
            }
            .map { result ->
                names = names + " second response: " + result.name
                Log.i(FLAT_MAP_OPERATOR, "second response")
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //setResult(names)
            }
    }

    //endregion

    //region COMBINING OBSERVABLES
    /**
     * This operator takes items from two or more observables and puts them into a single observable.
     * Example of parallel multiple network calls (asynchronous operations).
     */
    private fun mergeOperator() {
        disposable = Single.merge(
            remoteDataSource.getPlanet("1").subscribeOn(Schedulers.io()),
            remoteDataSource.getPlanet("2").subscribeOn(Schedulers.io()),
            remoteDataSource.getPlanet("3").subscribeOn(Schedulers.io()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.i(MERGE_OPERATOR, it.toString())
            }

        /*val observable1 = Observable.just("1","2","3")
        val observable2 = Observable.just("Hi", "Bye", "How are you?")
        val observable = Observable.merge(observable1, observable2)
            .subscribe {
                Log.i(MERGE_OPERATOR, it)
            }*/
    }

    /**
     * Perform two ways synchronous operations at the same time and only when both of
     * them are complete, can you move forward with the execution of the program.
     */
    private fun zipOperator() {
        /*disposable = Observable.zip(
            getObservablePeople("1"),
            getObservablePeople("2"),
            object : BiFunction<Person, Person, CombinePeople> {
                override fun apply(s: Person, s2: Person): CombinePeople {
                    return CombinePeople(s.name + " " + s2.name)
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showResult(it) },
                { showError(it) },
                { Log.d(ZIP_OPERATOR, "onComplete") },
                { Log.d(ZIP_OPERATOR, "onSubscribe") }
            )*/
    }

    /**
     *
     */
    private fun concatOperator() {
        val observable1 = Observable.just(1, 2, 3)
        val observable2 = Observable.just(4, 5, 6)
        Observable.concat(observable1, observable2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Int>{
                override fun onComplete() {
                    Log.d(CONCAT_OPERATOR, "onComplete")
                }
                override fun onSubscribe(d: Disposable) {
                    Log.d(CONCAT_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.d(CONCAT_OPERATOR, t.toString())
                }
                override fun onError(e: Throwable) {
                    Log.d(CONCAT_OPERATOR, "onError" + e.message)
                }
            })
    }

    //endregion

    //region FILTERING OBSERVABLES
    /**
     * https://reactivex.io/documentation/operators/filter.html
     */
    private fun filterOperator() {
        Observable.just(1,2,3,4, 5)
            .map { number -> number * number }
            .filter { it > 10 }
            .subscribe {
                Log.i(FILTER_OPERATOR, "onNext, $it")
            }
    }

    /**
     * https://reactivex.io/documentation/operators/skip.html
     */
    private fun skipOperator() {
        Observable
            .range(1, 10)
            .skip(4)
            .subscribe(object : Observer<Int>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(SKIP_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(SKIP_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(SKIP_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(SKIP_OPERATOR, "onError" + e.message)
                }
            })
    }

    /**
     * With this operator the decision on whether the item should be filtered out is made not
     * based on what the item is, but based on when the item was emitted.
     * Debounce operator only emit an item from an Observable if a particular time has passed
     * without it emitting another item.
     */
    private fun debounceOperator() {
        buttonEmitObservable
            .debounce(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.i(DEBOUNCE_OPERATOR, it.toString())
            }
    }

    /**
     * This operator suppress duplicate items emitted by an Observable.
     *
     * In order to work with a custom dataType, we need to override the equals() and
     * hashCode() methods.
     */
    private fun distinctOperator() {
        Observable.just("Hi","hi","hello","Cheers", "Wa!")
            .distinct()
            .subscribe {
                Log.i(DISTINCT_OPERATOR, it.toString())
            }
    }

    //endregion

    //region CONDITIONAL OPERATORS
    /**
     * Is used when you have two or more observables that they are going to return the same type
     * but you just wanna be notified as soon as the first one start emitting items.
     *
     * AMB subscribes both at the same time and waits for the first one to emit.
     *
     * If any of the operators are blocking, the other operators will get blocked and will not
     * executed and that blocking operation will complete, emitting that item. This defeats the
     * purpose of AMB.
     * So that means that all of your parameters to amb, which are observables, need to be
     * asynchronous for amb to work correctly.
     */
    private fun ambOperator() {
        /*disposable = Observable.ambArray(getObservablePeople("1"), getObservablePeople("2"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showResult(it) },
                { showError(it) },
                { Log.d(AMB_OPERATOR, "onComplete") },
                { Log.d(AMB_OPERATOR, "onSubscribe") })*/
    }

    //endregion

    //region UTILITY OPERATORS
    /**
     *
     */
    private fun delayOperator() {
        Observable.just("a", "b", "c")
            .delay(2, TimeUnit.SECONDS)
            .subscribe(object : Observer<String>{
                override fun onSubscribe(d: Disposable) {
                    Log.d(DELAY_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: String) {
                    Log.d(DELAY_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.d(DELAY_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.d(DELAY_OPERATOR, "onError" + e.message)
                }
            })
    }

    //endregion

    private fun getObservablePeople(number: String): Observable<People> {
        /*
        Just operator says: whatever the value placed as the argument to observable.just is,I'll turn that into an observable.
        This operator calls the onNext method and then immediately completes via onCompleted.

        However, this code has a problem, this code will get executed as soon as the observable is created.
        RxJava operators like Observable.just or Observable.from stored the value of the data in the operator when
        it's created, not when the operator has been subscribed to. We don't want that because for example the
        execution would happen on the main thread and block the UI.

        We describe how you can easily take any expensive method and wrap the call inside an RxJava Observable using
        the defer() operator. You can use Observable.defer() to wrap any method in an Observable so that we
        can defer the execution of an expensive method until the correct time, and so that we can control which
        threads are used to execute the method.

        return Observable.just(getPeople())
         */

        /*
        fromCallable/defer
        The operators are very similar and both can be used to solve the same type of problem: converting an
        expensive method call into an RxJava Observable.

        fromCallable can result in writing less code and provides better error handling.

        return Observable.fromCallable { getPeople() }
         */

        /*
        So we need to tell RxJava to defer the creation
        of the observable until we have someone who subscribes to it.
         */
        //return Observable.defer { Observable.just(getPerson(number)) }
        return remoteDataSource.getPeople(number)
    }

    //region REQUESTS

    /*private fun makeRequestWithRxJava() {

        *//*
    When OkHttp client receives a response from the server, it passes the response back to Retrofit.
    Retrofit then does its magic: it pushes the meaningless response bytes through converters and wraps
    it into a usable response with meaningful Java objects. This resource-intensive process is still
    done on a background thread. Finally, when everything is ready Retrofit needs to return the result
    to the UI thread of your Android app.
    The action of returning from the background thread, which receives and prepares
    the result, to the Android UI thread is a call adapter!
     *//*

        val retrofit = Retrofit.Builder()
            .baseUrl("https://swapi.dev")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SwApiService::class.java)

        disposable = service.getPersonObservable("1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showMessage("doOnSubscribe") }
            .doOnComplete { showMessage("doOnCompleted") }
            .subscribe(
                { showResult(it)}, { showError(it) }
            )
    }*/

    //endregion

    private fun setMessage(message: String) {
        binding.textResponse.text = message
    }
}
