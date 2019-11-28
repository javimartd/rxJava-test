package com.javimartd.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.javimartd.test.model.CombinePeople
import com.javimartd.test.model.People
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonMakeRequest.setOnClickListener {
            textResponse.text = ""
            makeSimpleRequestUsingRetrofitAndRxJava()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /* A memory leak like this can be caused by observables which retain a copy of the Android context.
        The problem commonly occurs when subscriptions are created that obtain a context somehow. */
        if (!disposable.isDisposed) {
            disposable.dispose()
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
        disposable = getPeopleObservable("1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it) }, { showError(it) })
    }

    private fun makeRequestUsingFromCallableOperator() {
        disposable = Observable.fromCallable { getPeople("1") }
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
        disposable = Observable.ambArray(getPeopleObservable("1"), getPeopleObservable("2"))
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
        disposable = Observable.zip(
            getPeopleObservable("1"),
            getPeopleObservable("2"),
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
        disposable = Observable.just(1,2,3)
            .map { (it * 2).toString() }
            .subscribe {item ->
                // onNext
                textResponse.text = "${textResponse.text}\n$item"
                Log.d("JustOperator", item)
            }

        Observable.just(1,2,3)
            .map { (it * 2).toString() }
            .subscribe(object: Observer<String> {
                override fun onComplete() {
                    Log.d("JustOperator", "onSubscribe")
                }

                override fun onSubscribe(d: Disposable) {
                    Log.d("JustOperator", "onSubscribe")
                }

                override fun onNext(t: String) {
                    textResponse.text = "${textResponse.text}\n$t"
                }

                override fun onError(e: Throwable) {
                    showError(e)
                }
            })
    }

    private fun getPeopleObservable(person: String): Observable<People> {
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

    private fun makeSimpleRequestUsingKotlin() {
        doAsync {
            val people = getPeople("1")
            uiThread {
                // Also we can use runOnUiThread { }
                textResponse.text = people?.name
            }
        }
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

    private fun makeSimpleRequestUsingRetrofitAndRxJava() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://swapi.co")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)

        disposable = service.getPeopleObservable("1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ showResult(it)}, { showError(it) })
    }

    private fun makeSimpleRequestUsingRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://swapi.co")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

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

    private fun showError(it: Throwable?) {
        Toast.makeText(this, it?.message, Toast.LENGTH_LONG).show()
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
}
