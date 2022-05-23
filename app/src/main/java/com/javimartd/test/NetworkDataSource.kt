package com.javimartd.test

import com.google.gson.Gson
import com.javimartd.test.api.People
import okhttp3.OkHttpClient
import okhttp3.Request

object NetworkDataSource {

    fun getPeople(person: String): People {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://swapi.dev/api/people/$person")
            .build()
        val response = client.newCall(request).execute()

        var people= People()
        if (response.isSuccessful) {
            people = Gson().fromJson(response.body()?.charStream(), People::class.java)
        }
        return people
    }
}