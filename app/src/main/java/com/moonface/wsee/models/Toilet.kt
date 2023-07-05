package com.moonface.wsee.models

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import java.util.Date

data class Location(val latitude: Float, val longitude: Float) {
    fun toLatLng(): LatLng {
        return LatLng(latitude.toDouble(), longitude.toDouble())
    }

    fun toAndroidLocation(): android.location.Location {
        val location = android.location.Location("")
        location.latitude = latitude.toDouble()
        location.longitude = longitude.toDouble()
        return location
    }

    companion object {
        fun fromAndroidLocation(location: android.location.Location): Location {
            return Location(location.latitude.toFloat(), location.longitude.toFloat())
        }

        fun fromLatLng(latLng: LatLng): Location {
            return Location(latLng.latitude.toFloat(), latLng.longitude.toFloat())
        }
    }
}

data class Cost(val amount: Float, val currency: String)

enum class ToiletOwnerType(val type: String) {
    PUBLIC("public"),
    RESTAURANT("restaurant"),
    HOTEL("hotel"),
    ATTRACTION("attraction"),
}

data class ToiletOwner(val name: String, val type: ToiletOwnerType)

data class Toilet(
    val cost: Cost? = null,
    val location: Location,
    val rating: Float? = null,
    @SerializedName("report_date") val reportDate: Date,
    val reporter: String,
    @SerializedName("website_url") val websiteUrl: String? = null,
    val owner: ToiletOwner? = null,
) {
    fun getTitle(): String {
        return owner?.name ?: "Public toilet"
    }

    fun getRatingDisplay(): String {
        return if (rating != null) "⭐".repeat(rating.toInt()) else "No rating"
    }
}
