package com.javimartd.test.model

import com.google.gson.annotations.SerializedName

data class Planet(
    @SerializedName("name")
    var name: String = "default_name",

    @SerializedName("climate")
    var climate: String = "default_climate",

    @SerializedName("terrain")
    var terrain: String = "default_terrain"
)