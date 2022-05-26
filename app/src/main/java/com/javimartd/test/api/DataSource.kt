package com.javimartd.test.api

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface DataSource {
    fun getPeople(number: String): Observable<People>
    fun getStarship(number: String): Flowable<Starship>
    fun getPlanet(number: String): Single<Planet>
}