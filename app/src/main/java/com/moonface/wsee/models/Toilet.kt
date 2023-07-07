package com.moonface.wsee.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.moonface.wsee.R
import java.util.Date

data class Location(val latitude: Float, val longitude: Float) {
    fun navigate(context: Context) {
        val gmmIntentUri =
            Uri.parse("google.navigation:q=${latitude},${longitude}&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        ContextCompat.startActivity(context, mapIntent, null)
    }

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

enum class ToiletOwnerType(val value: String) {
    @SerializedName("public")
    PUBLIC("public"),

    @SerializedName("restaurant")
    RESTAURANT("restaurant"),

    @SerializedName("hotel")
    HOTEL("hotel"),

    @SerializedName("attraction")
    ATTRACTION("attraction");

    fun getIcon(): Int {
        return when (value) {
            PUBLIC.value -> R.drawable.public_toilet
            RESTAURANT.value -> R.drawable.restaurant
            HOTEL.value -> R.drawable.hotel
            ATTRACTION.value -> R.drawable.attractions
            else -> throw Error("No such type")
        }
    }
}

data class ToiletOwner(val name: String, val type: ToiletOwnerType)

data class Toilet(
    val cost: Cost? = null,
    val location: Location,
    val rating: Float? = null,
    @SerializedName("review_count") var reviewCount: Int = 0,
    @SerializedName("report_date") val reportDate: Date,
    val reporter: String,
    @SerializedName("website_url") val websiteUrl: String? = null,
    val owner: ToiletOwner? = null,
) {
    fun getTitle(): String {
        return owner?.name ?: "Public toilet"
    }
}
