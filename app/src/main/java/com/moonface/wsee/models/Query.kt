package com.moonface.wsee.models

import com.google.gson.annotations.SerializedName

data class ToiletQuery(
    val location: Location,
    @SerializedName("limit_results") val limitResults: Int,
    @SerializedName("min_rating") val minRating: Float? = null
)
