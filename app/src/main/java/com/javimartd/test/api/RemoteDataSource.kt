package com.javimartd.test.api

import com.google.gson.Gson
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RemoteDataSource: DataSource {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://swapi.dev/api/")
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(SwApiService::class.java)

    override fun getPeople(number: String): Observable<People> {
        return service.getPeople(number)
    }

    override fun getStarship(number: String): Flowable<Starship> {
        return service.getStarship(number)
    }

    override fun getPlanet(number: String): Single<Planet> {
        return service.getPlanet(number)
    }

    fun getDummyPerson(number: String): People {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://swapi.dev/api/people/$number")
            .build()
        val response = client.newCall(request).execute()

        var person = People()
        if (response.isSuccessful) {
            person = Gson().fromJson(response.body?.charStream(), People::class.java)
        }
        return person
    }
}