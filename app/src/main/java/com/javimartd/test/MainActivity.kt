package com.javimartd.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.javimartd.test.model.People
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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
            makeRequestUsingRxJava()

            /*Observable.just("1", "2", "3")
                .subscribe { textResponse.text = "${textResponse.text}\n$it"}*/
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

    @SuppressLint("CheckResult")
    private fun makeRequestUsingRxJava() {
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

        /*subscription = Observable.fromCallable { getPeople() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it) }, { showError(it) })*/
    }

    private fun showError(it: Throwable?) {
        Toast.makeText(this, it?.message, Toast.LENGTH_LONG).show()
    }

    private fun showResult(it: People?) {
        textResponse.text = it?.name
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

    private fun getPeopleWithTryCatch(): Observable<People> {
        return try {
            Observable.just(getPeople())
        } catch (e: IOException) {
            //How the onError of a subscriber gets called via the Observable.error() method
            Observable.error(e)
        }
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
}
