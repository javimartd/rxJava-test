package com.javimartd.test.api

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface SwApiService {
    @GET("people/{number}")
    fun getPeople(@Path("number") number: String): Observable<People>

    @GET("starships/{number}")
    fun getStarship(@Path("number") number: String): Single<Starship>

    @GET("planets/{number}")
    fun getPlanet(@Path("number") number: String): Single<Planet>
}