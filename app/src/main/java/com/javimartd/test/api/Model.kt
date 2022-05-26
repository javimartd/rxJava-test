package com.javimartd.test.api

import com.google.gson.annotations.SerializedName

data class People(
    @SerializedName("name")
    var name: String = "default_name",

    @SerializedName("gender")
    val gender: String = "default_gender"
)

data class Planet(
    @SerializedName("name")
    var name: String = "default_name",

    @SerializedName("climate")
    var climate: String = "default_climate",

    @SerializedName("terrain")
    var terrain: String = "default_terrain"
)

data class Starship(
    @SerializedName("name")
    var name: String = "default_name",

    @SerializedName("model")
    val model: String = "default_model"
)