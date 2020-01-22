package com.javimartd.test

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.javimartd.test.model.CombinePeople
import com.javimartd.test.model.People
import com.javimartd.test.service.SwapiService
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * The building blocks of RxJava are:
 *
 * - Observable: class that emits a stream of data or events. i.e. a class that can be used to perform
 * some action, and publish the result.
 *
 * - Observer: class that receivers the events or data and acts upon it. i.e. a class that waits
 * and watches the Observable, and reacts whenever the Observable publishes results.
 *
 * The Observer has 4 interface methods to know the different states of the Observable.
 *
 * onSubscribe(): This method is invoked when the Observer is subscribed to the Observable.
 * onNext(): This method is called when a new item is emitted from the Observable.
 * onError(): This method is called when an error occurs and the emission of data is not successfully completed.
 * onComplete(): This method is called when the Observable has successfully completed emitting all items.
 *
 * object : Observer<People> {
    override fun onNext(t: People) {}
    override fun onSubscribe(d: Disposable) {}
    override fun onError(e: Throwable) {}
    override fun onComplete() {}
    }

 * A subscriber is something that subscribes to whatever the observable is returning.
 * It's like saying hey observable let me know when you get something via the onNext(),
 * when you error and when you're done via onCompleted()
 *
 * RxJava observables will either error out via onError or they will complete via onCompleted.
 * They both cannot be called, only one will be called. If an error occurs, onError gets
 * called, if an observable completes without error, onCompleted will get called.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val HOST = "https://swapi.co"
        const val CREATE_OPERATOR = "create_operator"
        const val DEFER_OPERATOR = "defer_operator"
        const val INTERVAL_OPERATOR = "interval_operator"
        const val FROM_OPERATOR = "from_operator"
        const val JUST_OPERATOR = "just_operator"
        const val RANGE_OPERATOR = "range_operator"
        const val TIMER_OPERATOR = "timer_operator"
        const val REPEAT_OPERATOR = "repeat_operator"
        const val DELAY_OPERATOR = "delay_operator"
        const val REPEAT_WHEN_OPERATOR = "repeat_when_operator"
        const val FLAT_MAP_OPERATOR = "flat_map_operator"
        const val ZIP_OPERATOR = "zip_operator"
        const val AMB_OPERATOR = "amb_operator"
    }

    private lateinit var disposable: Disposable
    private lateinit var compositeDisposable: CompositeDisposable

    private val alphabets = listOf("a", "b", "c", "d", "e", "f")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonMakeRequest.setOnClickListener {
            textResponse.text = ""
            repeatWhenOperator()
        }

        buttonStartActivity.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View) {
                startActivity<SecondActivity>()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        /* A memory leak like this can be caused by observables which retain a copy of the Android context.
        The problem commonly occurs when subscriptions are created that obtain a context somehow. */
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    //region OPERATORS
    /**
     * This operator creates an Observable from scratch by calling observer methods programmatically.
     */
    private fun createOperator() {
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
                    Log.i(CREATE_OPERATOR, e.message)
                }
                override fun onComplete() {
                    Log.i(CREATE_OPERATOR, "onComplete")
                }
            })
    }


    private fun flipOperator() { }

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
                { showMessage("onSubscribe")})
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
                    Log.d(INTERVAL_OPERATOR, e.message)
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
                    Log.i(FROM_OPERATOR, e.message)
                }
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
                    Log.d(JUST_OPERATOR, e.message)
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
                    Log.i(RANGE_OPERATOR, e.message)
                }
            })
    }

    /**
     * This operator creates an Observable that emits one particular item after a span of
     * time that you specify.
     *
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
                    Log.i(TIMER_OPERATOR, e.message)
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
                    Log.i(REPEAT_OPERATOR, e.message)
                }
            })
    }

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
                    Log.d(DELAY_OPERATOR, e.message)
                }
            })
    }

    private fun repeatWhenOperator() {

        // Observable.just("1")
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SwapiService::class.java)

        service.getObservablePeople("1")
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
                    Log.d(REPEAT_WHEN_OPERATOR, e.message)
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
        val service = retrofit.create(SwapiService::class.java)

        var names = ""
        disposable = service.getObservablePeople("1")
            .map {
                    firstResponse -> names = "first response: " + firstResponse.name
                Log.i("Continuations", "First response")
            }
            .flatMap {service.getObservablePeople("2") }
            .map {
                    result -> names = names + " second response: " + result.name
                Log.i("Continuations", "Second response")
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showResult(names) }
    }

    private fun fromCallableOperator() {
        disposable = Observable.fromCallable { getPeople("1") }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it) }, { showError(it) })
    }

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
    //endregion

    private fun getPeopleWithTryCatch(): Observable<People> {
        return try {
            Observable.just(getPeople("1"))
        } catch (e: IOException) {
            // how the onError of a subscriber gets called via the Observable.error() method
            Observable.error(e)
        }
    }

    //region REQUEST
    private fun makeRequestUsingKotlin() {
        doAsync {
            val people = getPeople("1")
            uiThread {
                // Also we can use runOnUiThread { }
                textResponse.text = people?.name
            }
        }
    }

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
        val service = retrofit.create(SwapiService::class.java)

        disposable = service.getObservablePeople("1")
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

        val service = retrofit.create(SwapiService::class.java)

        service.getPeople("1").enqueue(object: Callback<People> {
            override fun onFailure(call: Call<People>, t: Throwable) {
                Log.e("Retrofit", t.message)
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
            .url("https://swapi.co/api/people/$person")
            .build()
        val response = client.newCall(request).execute()

        var people: People? = null
        if (response.isSuccessful) {
            people = Gson().fromJson<People>(response.body()?.charStream(), People::class.java)
        }
        return people
    }
    //endregion

    // region other methods
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

    override fun onRestart() {
        super.onRestart()
        Log.i("MainActivity", "onRestart")
    }

    override fun onStart() {
        super.onStart()
        Log.i("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("MainActivity", "onStop")
    }
    //endregion

    private fun test() {
        val people = People("lucia", "female")
        val people2 = People("lucia", "female")
        val result = people.equals(people2)
        textResponse.text = result.toString()
    }
}
