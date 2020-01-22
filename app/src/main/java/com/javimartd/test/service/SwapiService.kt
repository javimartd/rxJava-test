package com.javimartd.test.service

import com.javimartd.test.model.People
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SwapiService {

    @GET("/api/people/{person}")
    fun getPeople(@Path("person") person: String): Call<People>

    @GET("/api/people/{person}")
    fun getObservablePeople(@Path("person") person: String): Observable<People>

    @GET("/api/people/{person}")
    fun getSinglePeople(@Path("person") person: String): Single<People>

    @GET("/api/people/{person}")
    fun getFlowablePeople(@Path("person") person: String): Flowable<People>
}