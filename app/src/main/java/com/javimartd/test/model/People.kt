package com.javimartd.test.model

import com.google.gson.annotations.SerializedName

data class People(
        @SerializedName("name")
        var name: String = "default_name",

        @SerializedName("gender")
        val gender: String = "default_gender"
)