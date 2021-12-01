package com.javimartd.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.javimartd.test.model.CombinePeople
import com.javimartd.test.model.People
import com.javimartd.test.service.SwApiService
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * RxJava makes use of the Observer pattern.
 *
 * In the Observer pattern, you have objects that implement two key RxJava interfaces:
 * Observable and Observer. When an Observable changes state, all Observer objects subscribed
 * to it are notified.
 *
 * - Observable: class that emits a stream of data or events. i.e. a class that can be used to perform
 * some action, and publish the result.
 *
 * - Observer: class that receivers the events or data and acts upon it. i.e. a class that waits
 * and watches the Observable, and reacts whenever the Observable publishes results.
 *
 * The Observer has 4 interface methods to know the different states of the Observable.
 * onSubscribe(): this method is invoked when the Observer is subscribed to the Observable.
 * onNext(): is called when a new item is emitted from the Observable.
 * onError(): is called when an error occurs and the emission of data is not successfully completed.
 * onComplete(): is called when the Observable has successfully completed emitting all items.
 *
 * object : Observer<People> {
    override fun onNext(t: People) {}
    override fun onSubscribe(d: Disposable) {}
    override fun onError(e: Throwable) {}
    override fun onComplete() {}
    }

 * A subscriber is something that subscribes to whatever the observable is returning.
 * It's like saying "hey observable let me know when you get something via the onNext(),
 * when you error and when you're done via onCompleted()"
 *
 * RxJava observables will either error out via onError or they will complete via onCompleted.
 * They both cannot be called, only one will be called. If an error occurs, onError gets
 * called, if an observable completes without error, onCompleted will get called.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val HOST = "https://swapi.dev"

        // creating observables
        const val CREATE_OPERATOR = "create_operator"
        const val FROM_CALLABLE_OPERATOR = "from_callable_operator"
        const val JUST_OPERATOR = "just_operator"
        const val DEFER_OPERATOR = "defer_operator"
        const val INTERVAL_OPERATOR = "interval_operator"
        const val RANGE_OPERATOR = "range_operator"
        const val FROM_OPERATOR = "from_operator"
        const val TIMER_OPERATOR = "timer_operator"
        const val REPEAT_OPERATOR = "repeat_operator"
        const val REPEAT_WHEN_OPERATOR = "repeat_when_operator"

        // transforming observables
        const val FLAT_MAP_OPERATOR = "flat_map_operator"
        const val MAP_OPERATOR = "map_operator"
        const val GROUP_BY_OPERATOR = "group_by_operator"

        // combining observables
        const val MERGE_OPERATOR = "merge_operator"
        const val ZIP_OPERATOR = "zip_operator"
        const val JOIN_OPERATOR = "join_operator"

        // filtering observables
        const val FILTER_OPERATOR = "filter_operator"
        const val DEBOUNCE_OPERATOR = "debounce_operator"
        const val DISTINCT_OPERATOR = "distint_operator"
        const val SAMPLE_OPERATOR = "sample_operator"
        const val FIRST_OPERATOR = "first_operator"
        const val SKIP_OPERATOR = "skip_operator"
        const val TAKE_OPERATOR = "take_operator"
        const val TAKE_LAST_OPERATOR = "take_last_operator"

        // conditional operators
        const val AMB_OPERATOR = "amb_operator"

        // utility operators
        const val DELAY_OPERATOR = "delay_operator"
    }

    private lateinit var disposable: Disposable
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var buttonEmitObservable: Observable<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonOperator.setOnClickListener {
            textResponse.text = ""
            mergeOperator()
        }

        buttonEmitObservable = createButtonClickObservable()

        buttonStartActivity.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(MainActivity::class.java.name, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(MainActivity::class.java.name, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(MainActivity::class.java.name, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        // A memory leak like this can be caused by observables which retain a copy of the Android context.
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    private fun createButtonClickObservable(): Observable<Int> {
        var counter = 0
        return Observable.create { emitter ->
            buttonEmit.setOnClickListener {
                emitter.onNext(counter)
                counter++
                //disposable.dispose()
            }
            // setCancelable() method won’t fire until the observable is unsubscribed.
            emitter.setCancellable {
                buttonOperator.setOnClickListener(null)
            }
        }
    }

    private fun consumerInterface() {
        disposable = buttonEmitObservable.subscribe(object : Consumer<Int> {
            override fun accept(t: Int?) {
                showMessage(t.toString())
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun simpleSubscription() {
        // This interface is handy when you want to set up a simple subscription to an Observable.
        Observable.just("This is an example")
            .subscribe(object: Consumer<String> {
                override fun accept(t: String?) {
                    Log.i(MainActivity::class.java.name, t.toString())
                }
            })
    }

    //region CREATING OBSERVABLES
    /**
     * This operator creates an Observable from scratch by calling observer methods programmatically.
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
                    Log.i(CREATE_OPERATOR, s)
                }
                override fun onError(e: Throwable) {
                    Log.i(CREATE_OPERATOR, e.message!!)
                }
                override fun onComplete() {
                    Log.i(CREATE_OPERATOR, "onComplete")
                }
            })
    }

    @SuppressLint("CheckResult")
    private fun fromCallableOperator() {
        Observable.fromCallable<String> {
            // do something and return
            return@fromCallable "Hi!"
        }
            .subscribeOn(Schedulers.io())
            .subscribe {
                Log.d(FROM_CALLABLE_OPERATOR, it)
            }
    }

    /**
     * This operator does not create the Observable until the Observer subscribes.
     * The only downside to defer() is that it creates a new Observable each time you
     * get a new Observer.
     */
    private fun deferOperator() {
        disposable = getObservablePeople("1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showResult(it) },
                { showError(it) },
                { showMessage("onCompleted") },
                { showMessage("onSubscribe")
                    Log.i(DEFER_OPERATOR, "onSubscribe")
                })
    }

    /**
     * This operator takes a list of arguments (maximum 10) and converts the items into
     * Observable items. just() makes only 1 emission. For instance, If an array is passed as
     * a parameter to the just() method, the array is emitted as single item instead of
     * individual numbers. Note that if you pass null to just(), it will return an Observable
     * that emits null as an item.
     */
    private fun justOperator() {
        /*disposable = Observable.just(1,2,3)
            .map { (it * 2).toString() }
            .subscribe {item ->
                // onNext
                textResponse.text = "${textResponse.text}\n$item"
                Log.d("JustOperator", item)
            }*/

        Observable.just(1,2,3,4)
            .map { (it * 2).toString() }
            .subscribe(object: Observer<String> {
                override fun onComplete() {
                    Log.d(JUST_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.d(JUST_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: String) {
                    Log.d(JUST_OPERATOR, "${textResponse.text}\n$t")
                }

                override fun onError(e: Throwable) {
                    Log.d(JUST_OPERATOR, e.toString())
                }
            })
    }

    /**
     * This operator creates an Observable that emits a sequence of integers spaced by a
     * particular time interval.
     *
     * Observable.interval() and Observable.timer()
     * timer() emits just a single item after a delay whereas
     * interval() operator, on the other hand, will emit items spaced out with a given interval.
     */
    private fun intervalOperator() {
        Observable.interval(1, TimeUnit.SECONDS)
            .take(10)
            .subscribe(object: Observer<Long>{
                override fun onComplete() {
                    Log.d(INTERVAL_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.d(INTERVAL_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: Long) {
                    Log.d(INTERVAL_OPERATOR, t.toString())
                }

                override fun onError(e: Throwable) {
                    Log.d(INTERVAL_OPERATOR, e.toString())
                }
            })
    }

    /**
     * This operator creates an Observable that emits one particular item after a span of
     * time that you specify.
     *
     * Difference between Observable.interval() and Observable.timer()
     * — timer() emits just a single item after a delay
     * - interval() operator, on the other hand, will emit items spaced out with a given interval.
     */
    private fun timerOperator() {
        Observable.timer(3, TimeUnit.SECONDS)
            .subscribe(object : Observer<Long> {
                override fun onComplete() {
                    Log.i(TIMER_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.i(TIMER_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: Long) {
                    Log.i(TIMER_OPERATOR, t.toString())
                }

                override fun onError(e: Throwable) {
                    Log.i(TIMER_OPERATOR, e.toString())
                }
            })
    }

    /**
     * This operator creates an Observable from set of items using an Iterable, which means
     * we can pass a list or an array of items to the Observable and each item is emitted
     * one at a time. Some of the examples of the operators include fromCallable(),
     * fromFuture(), fromIterable(), fromPublisher(), fromArray().
     */
    private fun fromOperator() {
        Observable.fromArray(arrayOf("A", "B", "C", "D", "E", "F"))
            .subscribe(object : Observer<Array<String>>{
                override fun onComplete() {
                    Log.i(FROM_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.i(FROM_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: Array<String>) {
                    Log.i(FROM_OPERATOR, t.toString())
                }

                override fun onError(e: Throwable) {
                    Log.i(FROM_OPERATOR, e.toString())
                }
            })
    }

    /**
     * This operator creates an Observable that emits a particular item or sequence of items
     * repeatedly. There is an option to pass the number of repetitions that can take place as well.
     */
    private fun repeatOperator() {
        Observable.just(1,2, 3)
            .repeat(3)
            .subscribe(object : Observer<Int> {
                override fun onComplete() {
                    Log.i(REPEAT_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.i(REPEAT_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: Int) {
                    Log.i(REPEAT_OPERATOR, t.toString())
                }

                override fun onError(e: Throwable) {
                    Log.i(REPEAT_OPERATOR, e.toString())
                }
            })
    }

    /**
     * This operator creates an Observable that emits a range of sequential integers.
     * The function takes two arguments: the starting number and length.
     */
    private fun rangeOperator() {
        Observable.range(2,10)
            .subscribe(object: Observer<Int>{
                override fun onComplete() {
                    Log.i(RANGE_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.i(RANGE_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: Int) {
                    Log.i(RANGE_OPERATOR, t.toString())
                }

                override fun onError(e: Throwable) {
                    Log.i(RANGE_OPERATOR, e.toString())
                }
            })
    }

    //endregion

    //region TRANSFORMING OBSERVABLES
    /**
     * This operator applies a function to each item emitted by an observable and returns
     * another observable
     */
    @SuppressLint("CheckResult")
    private fun mapOperator() {
        Observable.just(1,2,3)
            .map { number -> number * number }
            .subscribe {
                Log.i(MAP_OPERATOR, it.toString())
            }
    }

    /**
     * The flatMap operator help you to transform one event to another Observable
     * (or transform an event to zero, one, or more events). It's a perfect operator when
     * you want to call another method which return an Observable.
     */
    private fun flatMapOperator() {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SwApiService::class.java)

        var names = ""
        disposable = service.getPeople_Observable("1")
            .map {
                    firstResponse -> names = "first response: " + firstResponse.name
                Log.i(FLAT_MAP_OPERATOR, "First response")
            }
            .flatMap {
                service.getPeople_Observable("2")
            }
            .map { result ->
                names = names + " second response: " + result.name
                Log.i(FLAT_MAP_OPERATOR, "Second response")
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showResult(names) }
    }

    //endregion

    //region COMBINING OBSERVABLES
    /**
     * This operator takes items from two or more observables and puts them into a single observable
     */
    private fun mergeOperator() {
        val observable1 = Observable.just("1","2","3")
        val observable2 = Observable.just("Hi", "Bye", "How are you?")
        val observable = Observable.merge<String>(observable1, observable2)
            .subscribe {
                Log.i(MERGE_OPERATOR, it)
            }
    }

    /**
     * You'll need to perform two ways synchronous operations at the same time and only when both of
     * them are complete, can you move forward with the execution of the program.
     *
     * This operator allows you to combine a set of items that have been emitted by two or more
     * observables via a special function. When that happens the zip operator will emit the
     * items based upon this function.
     */
    private fun zipOperator() {
        disposable = Observable.zip(
            getObservablePeople("1"),
            getObservablePeople("2"),
            object : BiFunction<People, People, CombinePeople> {
                override fun apply(s: People, s2: People): CombinePeople {
                    return CombinePeople(s.name + " " + s2.name)
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showResult(it) },
                { showError(it) },
                { Log.d(ZIP_OPERATOR, "onComplete") },
                { Log.d(ZIP_OPERATOR, "onSubscribe") })
    }

    //endregion

    //region FILTERING OBSERVABLES
    /**
     * With this operator the decision on whether the item should be filtered out is made not
     * based on what the item is, but based on when the item was emitted.
     * Debounce operator only emit an item from an Observable if a particular time has passed
     * without it emitting another item.
     */
    @SuppressLint("CheckResult")
    private fun debounceOperator() {
        buttonEmitObservable
            .debounce(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.i(DEBOUNCE_OPERATOR, it.toString())
            }
    }

    /**
     * This operator passes only those items which satisfy a particular condition
     */
    @SuppressLint("CheckResult")
    private fun filterOperator() {
        Observable.just(1,2,3,4)
            .map { number -> number * number }
            .filter { it > 5 }
            .subscribe {
                Log.i(FILTER_OPERATOR, it.toString())
            }
    }

    /**
     * This operator suppress duplicate items emitted by an Observable.
     *
     * In order to work with a custom dataType, we need to override the equals() and
     * hashCode() methods.
     */
    @SuppressLint("CheckResult")
    private fun distinctOperator() {
        Observable.just("Hi","hi","hello","Cheers", "Wa!")
            .distinct()
            .subscribe {
                Log.i(DISTINCT_OPERATOR, it.toString())
            }
    }

    //endregion

    //region CONDITIONAL OPERATORS
    private fun ambOperator() {
        /*
        Is used when you have two or more observables that they are going to return the same type but
        you just wanna be notified as soon as the first one start emitting items.

        AMB subscribes both at the same time and waits for the first one to emit.

        If any of the operators are blocking, the other operators will get blocked and will not executed
        and that blocking operation will complete, emitting that item. This defeats the purpose of AMB.

        So that means that all of your parameters to amb, which are observables, need to be asynchronous
        for amb to work correctly.
         */
        disposable = Observable.ambArray(getObservablePeople("1"), getObservablePeople("2"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showResult(it) },
                { showError(it) },
                { Log.d(AMB_OPERATOR, "onComplete") },
                { Log.d(AMB_OPERATOR, "onSubscribe") })
    }

    //endregion

    //region UTILITY OPERATORS
    private fun delayOperator() {
        Observable.just("a", "b", "c")
            .delay(2, TimeUnit.SECONDS)
            .subscribe(object : Observer<String>{
                override fun onComplete() {
                    Log.d(DELAY_OPERATOR, "onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.d(DELAY_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: String) {
                    Log.d(DELAY_OPERATOR, t)
                }

                override fun onError(e: Throwable) {
                    Log.d(DELAY_OPERATOR, e.toString())
                }
            })
    }

    //endregion

    private fun flipOperator() { }

    private fun repeatWhenOperator() {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SwApiService::class.java)

        service.getPeople_Observable("1")
            .repeatWhen {completed -> completed.delay(2, TimeUnit.SECONDS)}
            .doOnComplete { Log.i("repeatWhenOperator", "doOnComplete") }
            .doOnNext { Log.i("repeatWhenOperator", "doOnNext") }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<People>{
                override fun onSubscribe(d: Disposable) {
                    Log.d(REPEAT_WHEN_OPERATOR, "onSubscribe")
                }

                override fun onNext(t: People) {
                    Log.d(REPEAT_WHEN_OPERATOR, t.toString())
                }

                override fun onError(e: Throwable) {
                    Log.d(REPEAT_WHEN_OPERATOR, e.toString())
                }

                override fun onComplete() {
                    Log.d(REPEAT_WHEN_OPERATOR, "onComplete")
                }
            })

             /*.subscribe(object : FlowableSubscriber<People>{
                 override fun onComplete() {
                     Log.d(REPEAT_WHEN_OPERATOR, "onComplete")
                 }

                 override fun onSubscribe(s: Subscription) {
                     Log.d(REPEAT_WHEN_OPERATOR, "onSubscribe")
                 }

                 override fun onNext(t: People?) {
                     Log.d(REPEAT_WHEN_OPERATOR, t.toString())
                 }

                 override fun onError(t: Throwable?) {
                     Log.d(REPEAT_WHEN_OPERATOR, t?.message)
                 }
             })*/
    }

    private fun getObservablePeople(person: String): Observable<People> {
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
        return Observable.defer { Observable.just(getPeople(person)) }
    }

    private fun getPeopleWithTryCatch(): Observable<People> {
        return try {
            Observable.just(getPeople("1"))
        } catch (e: IOException) {
            // how the onError of a subscriber gets called via the Observable.error() method
            Observable.error(e)
        }
    }

    //region REQUESTS

    private fun makeRequestUsingRetrofitAndRxJava() {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST)
                /*
                When OkHttp client receives a response from the server, it passes the response back to Retrofit.
                Retrofit then does its magic: it pushes the meaningless response bytes through converters and wraps
                it into a usable response with meaningful Java objects. This resource-intensive process is still
                done on a background thread. Finally, when everything is ready Retrofit needs to return the result
                to the UI thread of your Android app.
                The action of returning from the background thread, which receives and prepares
                the result, to the Android UI thread is a call adapter!
                 */
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SwApiService::class.java)

        disposable = service.getPeople_Observable("1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {showMessage("doOnSubscribe")}
            .doOnComplete { showMessage("doOnCompleted") }
            .subscribe({ showResult(it)}, { showError(it) })
    }

    private fun makeRequestUsingRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SwApiService::class.java)

        service.getPeople("1").enqueue(object: Callback<People> {
            override fun onFailure(call: Call<People>, t: Throwable) {
                Log.e("Retrofit", t.toString())
            }

            override fun onResponse(call: Call<People>, response: Response<People>) {
                Log.d("Retrofit", response.message())
                if (response.isSuccessful) {
                    textResponse.text = response.body()?.name
                }
            }
        })
    }

    private fun getPeople(person: String): People? {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://swapi.dev/api/people/$person")
            .build()
        val response = client.newCall(request).execute()

        var people= People()
        if (response.isSuccessful) {
            people = Gson().fromJson<People>(response.body()?.charStream(), People::class.java)
        }
        return people
    }
    //endregion

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(it: Throwable?) {
        Toast.makeText(this, it?.message, Toast.LENGTH_SHORT).show()
    }

    private fun showResult(message: String) {
        textResponse.text = message
    }

    private fun showResult(it: People?) {
        textResponse.text = it?.name
    }

    private fun showResult(it: CombinePeople?) {
        textResponse.text = it?.compoundName
    }
}
