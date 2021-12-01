package com.javimartd.test.service

import com.javimartd.test.model.People
import com.javimartd.test.model.Planet
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SwApiService {

    @GET("/api/people/{person}")
    fun getPeople(@Path("person") person: String): Call<People>

    @GET("/api/people/{person}")
    fun getPeople_Observable(@Path("person") person: String): Observable<People>

    /*@GET("/api/people/{person}")
    fun getPeople(@Path("person") person: String): Single<People>

    @GET("/api/people/{person}")
    fun getPeople(@Path("person") person: String): Flowable<People>*/

    @GET("/api/planets/{planetNumber}")
    fun getPlanet(@Path("planetNumber") planetNumber: String): Single<Planet>
}