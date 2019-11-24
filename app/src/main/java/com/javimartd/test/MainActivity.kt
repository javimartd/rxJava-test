package com.javimartd.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.javimartd.test.model.CombinePeople
import com.javimartd.test.model.People
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var subscription: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonMakeRequest.setOnClickListener {
            textResponse.text = ""

            //makeRequestUsingDeferOperator()
            makeRequestUsingZipOperator()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /*
        NOTE: A memory leak like this can be caused by observables which retain a copy of the Android context.
        The problem commonly occurs when subscriptions are created that obtain a context somehow.
         */
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }

    private fun makeRequestUsingDeferOperator() {
        /*
        A subscriber is something that subscribes to whatever the observable is returning.
        It's like saying hey observable let me know when you get something via the onNext(), when you
        error and when you're done via onCompleted()

        RxJava observables will either error out via onError or they will complete via onCompleted.
        They both cannot be called, only one will be called. If an error occurs, onError gets called, if
        an observable completes without error, onCompleted will get called.
         */
        subscription = getPeopleObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it) }, { showError(it) })
    }

    private fun makeRequestUsingFromCallableOperator() {
        subscription = Observable.fromCallable { getPeople() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it) }, { showError(it) })
    }

    private fun makeRequestUsingAmbOperator() {
        /*
        Is used when you have two or more observables that they are going to return the same type but
        you just wanna be notified as soon as the first one start emitting items.

        AMB subscribes both at the same time and waits for the first one to emit.

        If any of the operators are blocking, the other operators will get blocked and will not executed
        and that blocking operation will complete, emitting that item. This defeats the purpose of AMB.

        So that means that all of your parameters to amb, whick are observables, need to be asynchronous
        for amb to work correctly.
         */
        subscription = Observable.ambArray(getPeopleObservable(), getPeople2Observable())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it) }, { showError(it) })
    }

    private fun makeRequestUsingZipOperator() {
        /*
        You'll need to perform two ways synchronous operations at the same time and only when both of
        them are complete, can you move forward with the execution of the program.

        This operator allows you to combine a set of items that have been emitted by two or more observables via a
        special function. When that happens the zip operator will emit the items based upon this function.
         */
        subscription = Observable.zip(
            getPeople2Observable(),
            getPeopleObservable(),
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
            { Log.d("ZipOperator", "onComplete") },
            { Log.d("ZipOperator", "onSubscribe") })
    }

    private fun makeRequestUsingJustOperator() {
        subscription = Observable.just("1", "2", "3")
            .subscribe { textResponse.text = "${textResponse.text}\n$it"}
    }

    private fun getPeopleObservable(): Observable<People> {
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
        return Observable.defer { Observable.just(getPeople()) }
    }

    private fun getPeople2Observable(): Observable<People> {
        return Observable.defer { Observable.just(getPeople2()) }
    }

    private fun getPeopleWithTryCatch(): Observable<People> {
        return try {
            Observable.just(getPeople())
        } catch (e: IOException) {
            // how the onError of a subscriber gets called via the Observable.error() method
            Observable.error(e)
        }
    }

    private fun showError(it: Throwable?) {
        Toast.makeText(this, it?.message, Toast.LENGTH_LONG).show()
    }

    private fun showResult(it: People?) {
        textResponse.text = it?.name
    }

    private fun showResult(it: CombinePeople?) {
        textResponse.text = it?.compoundName
    }

    private fun makeRequestUsingKotlin() {
        doAsync {
            val people = getPeople()
            uiThread {
                // Also we can use runOnUiThread { }
                textResponse.text = people?.name
            }
        }
    }

    private fun getPeople(): People? {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://swapi.co/api/people/1")
            .build()
        val response = client.newCall(request).execute()

        var people: People? = null
        if (response.isSuccessful) {
            people = Gson().fromJson<People>(response.body()?.charStream(), People::class.java)
        }
        return people
    }

    private fun getPeople2(): People? {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://swapi.co/api/people/2")
            .build()
        val response = client.newCall(request).execute()

        var people: People? = null
        if (response.isSuccessful) {
            people = Gson().fromJson<People>(response.body()?.charStream(), People::class.java)
        }
        return people
    }
}
