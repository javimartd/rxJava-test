package com.javimartd.test.api

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SwApiService {

    @GET("/api/people/{person}")
    fun getPeople(@Path("person") person: String): Call<People>

    @GET("/api/people/{person}")
    fun getPeopleObservable(@Path("person") person: String): Observable<People>

    @GET("/api/people/{person}")
    fun getPeopleSingle(@Path("person") person: String): Single<People>

    @GET("/api/people/{person}")
    fun getPeopleFlowable(@Path("person") person: String): Flowable<People>

    @GET("/api/planets/{planetNumber}")
    fun getPlanet(@Path("planetNumber") planetNumber: String): Single<Planet>
}