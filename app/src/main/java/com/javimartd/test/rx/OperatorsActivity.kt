package com.javimartd.test.rx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.javimartd.test.EmptyActivity
import com.javimartd.test.databinding.ActivityOperatorsBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import android.R.integer
import com.javimartd.test.api.*


class OperatorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperatorsBinding

    private lateinit var disposable: Disposable
    private lateinit var compositeDisposable: CompositeDisposable

    private lateinit var remoteDataSource: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperatorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonOperator.setOnClickListener {
            groupByOperator()
        }
        binding.buttonCancel.setOnClickListener {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        binding.buttonStartActivity.setOnClickListener {
            startActivity(Intent(this, EmptyActivity::class.java))
        }

        createObservableButton()

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

    private fun setMessage(message: String) {
        binding.textResponse.text = message
    }

    private fun createObservableButton() {
        var counter = 0
        disposable = Observable.create<Int> { emitter ->
            binding.buttonObservable.setOnClickListener {
                emitter.onNext(counter)
                counter++
            }
            emitter.setCancellable {
                // setCancelable method will fire when the observable is unsubscribed.
                binding.buttonObservable.setOnClickListener(null)
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
                    Log.i(CREATE_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/just.html
     */
    private fun justOperator() {
        Observable.just(1,2,3,4,5)
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
                    Log.i(JUST_OPERATOR, "onError, ${e.message}")
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
                    Log.i(FROM_OPERATOR, "onError, ${e.message}")
                }
            })
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
                    Log.i(TIMER_OPERATOR, "onError, ${e.message}")
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
                    Log.i(INTERVAL_OPERATOR, "onError, ${e.message}")
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
                    Log.i(RANGE_OPERATOR, "onError, ${e.message}")
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
                    Log.i(REPEAT_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    private fun repeatWhenOperator() {
        remoteDataSource.getPeople("2")
            .repeatWhen { completed -> completed.delay(2, TimeUnit.SECONDS) }
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
                    Log.i(REPEAT_WHEN_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/defer.html
     */
    private fun deferOperator() {
        var film = "Episode V – The Empire Strikes "
        val deferObservable = Observable.defer { Observable.just(film) }
        film = "Episode IV – A New Hope"
        deferObservable.subscribe {
            Log.i(DEFER_OPERATOR, "onNext, $it")
        }
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

    /**
     * https://reactivex.io/documentation/operators/groupby.html
     */
    private fun groupByOperator() {
        val observable = Observable.just(
            People("Luke", "male"),
            People ("C130", "N/A"),
            People ("Pade", "female")
        )
        observable
            .groupBy { it.gender == "male" }
            .subscribe {
                if (it.key == true) {
                    it.subscribe { people ->
                        Log.i(GROUP_BY_OPERATOR, "onNext, $people")
                    }
                }
            }
    }

    //endregion

    //region COMBINING OBSERVABLES
    /**
     * https://reactivex.io/documentation/operators/merge.html
     */
    private fun mergeOperator() {
        val firstObservable = Observable
            .interval(1000, TimeUnit.MILLISECONDS)
            .take(5)
        val secondObservable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .take(10)

        Observable.merge(
            firstObservable,
            secondObservable
        )
            .subscribe(object: Observer<Any> {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                    Log.d(MERGE_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Any) {
                    Log.d(MERGE_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.d(MERGE_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.d(MERGE_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * Perform two ways synchronous operations at the same time and only when both of
     * them are complete, can you move forward with the execution of the program.
     */
    private fun zipOperator() {

        val single1 = remoteDataSource.getStarship("1")
        val single2 = remoteDataSource.getPlanet("5")

        single1.zipWith(single2, BiFunction { s1: Starship, s2: Planet -> "$s1 $s2" })

        /*Observable.pa(
            remoteDataSource.getPeople("5"),
            remoteDataSource.getStarship("2"),
            object : BiFunction<People, Starship, (r1, r2)> {
                override fun apply(s: People, s2: Starship): CombinePeople {
                    return CombinePeople(s.name + " " + s2.name)
                }
            }
        )*/
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(

            )
    }

    /**
     * https://reactivex.io/documentation/operators/concat.html
     */
    private fun concatOperator() {
        val firstObservable = Observable.just(1, 2, 3)
        val secondObservable = Observable.just("a", "b", "c", "d", "e")
        val thirdObservable3 = Observable.just(4, 5, 6)

        Observable.concat(
            firstObservable,
            secondObservable,
            thirdObservable3
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Any>{
                override fun onSubscribe(d: Disposable) {
                    Log.i(CONCAT_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Any) {
                    Log.i(CONCAT_OPERATOR, "onNext, $t")
                }
                override fun onComplete() {
                    Log.i(CONCAT_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(CONCAT_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/combinelatest.html
     */
    private fun combineLatestOperator() {

        val firstObservable = Observable.just(1,2,3)
        val secondObservable = Observable.just(4,5,6)

        Observable.combineLatest(firstObservable, secondObservable) { first, second ->
            "$first $second"
        }.subscribe(object: Observer<Any>{
            override fun onSubscribe(d: Disposable) {
                Log.i(COMBINE_LATEST_OPERATOR, "onSubscribe")
            }
            override fun onNext(t: Any) {
                Log.i(COMBINE_LATEST_OPERATOR, "onNext, $t")
            }
            override fun onComplete() {
                Log.i(COMBINE_LATEST_OPERATOR, "onComplete")
            }
            override fun onError(e: Throwable) {
                Log.i(COMBINE_LATEST_OPERATOR, "onError, ${e.message}")
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
                    Log.i(SKIP_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/first.html
     */
    private fun firstOperator() {
        Observable.just(1, 2, 3)
            .first(0)
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(FIRST_OPERATOR, "onSubscribe")
                }
                override fun onSuccess(t: Int) {
                    Log.i(FIRST_OPERATOR, "onSuccess, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(FIRST_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/last.html
     */
    private fun lastOperator() {
        Observable.just(1, 2, 3)
            .last(0)
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(LAST_OPERATOR, "onSubscribe")
                }
                override fun onSuccess(t: Int) {
                    Log.i(LAST_OPERATOR, "onSuccess, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(LAST_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/debounce.html
     */
    private fun debounceOperator() {
        Observable
            .interval(4, TimeUnit.SECONDS)
            .take(20)
            .debounce(2, TimeUnit.SECONDS)
            .subscribe(object: Observer<Long>{
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                    Log.i(DEBOUNCE_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Long) {
                    Log.i(DEBOUNCE_OPERATOR, "onNext, $t")
                }
                override fun onError(e: Throwable) {
                    Log.i(DEBOUNCE_OPERATOR, "onError, ${e.message}")
                }
                override fun onComplete() {
                    Log.i(DEBOUNCE_OPERATOR, "onComplete")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/distinct.html
     */
    private fun distinctOperator() {
        Observable.just(1,2,2,3,3,3,4,5)
            .distinct()
            .subscribe {
                Log.i(DISTINCT_OPERATOR, it.toString())
            }
    }

    /**
     * https://reactivex.io/documentation/operators/take.html
     */
    private fun takeOperator() {
        Observable.just(1, 2, 3, 4,5,6,7, 8)
            .take(4)
            .subscribe(object : Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(TAKE_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Int) {
                    Log.i(TAKE_OPERATOR, "onNext $t")
                }
                override fun onComplete() {
                    Log.i(TAKE_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(TAKE_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    /**
     * https://reactivex.io/documentation/operators/sample.html
     */
    private fun sampleOperator() {
        Observable.interval(1, TimeUnit.SECONDS)
            .take(20)
            .sample(5000, TimeUnit.MILLISECONDS )
            .subscribe(object: Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(SAMPLE_OPERATOR, "onSubscribe")
                }
                override fun onNext(t: Long) {
                    Log.i(SAMPLE_OPERATOR, "onNext $t")
                }
                override fun onComplete() {
                    Log.i(SAMPLE_OPERATOR, "onComplete")
                }
                override fun onError(e: Throwable) {
                    Log.i(SAMPLE_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    //endregion

    //region UTILITY OPERATORS
    /**
     * https://reactivex.io/documentation/operators/delay.html
     */
    private fun delayOperator() {
        Observable.just("a", "b", "c", "d", "e")
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
                    Log.d(DELAY_OPERATOR, "onError, ${e.message}")
                }
            })
    }

    //endregion
}
