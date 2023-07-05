package com.moonface.wsee.toilet_repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.moonface.wsee.models.Toilet
import com.moonface.wsee.models.ToiletQuery
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


interface ToiletApi {
    @POST("toilets/search")
    suspend fun search(@Body query: ToiletQuery): List<Toilet>
}

class ToiletRepository : IToiletRepository {
    private val api: ToiletApi by lazy {
        createApi()
    }

    override suspend fun search(query: ToiletQuery): List<Toilet> {
        return api.search(query)
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            .create()
    }

    private fun createApi(): ToiletApi {
        val gsonConverterFactory = GsonConverterFactory.create(createGson())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://wsee-617a454e18fb.herokuapp.com/")
            .addConverterFactory(gsonConverterFactory)
            .build()

        return retrofit.create(ToiletApi::class.java)
    }
}