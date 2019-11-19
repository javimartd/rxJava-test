package com.javimartd.test.model

import com.google.gson.annotations.SerializedName

data class People(
        @SerializedName("name")
        val name: String = "",

        @SerializedName("gender")
        val gender: String = "")