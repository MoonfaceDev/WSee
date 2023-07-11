package com.moonface.wsee.toilet_repository

import com.moonface.wsee.models.CreateReview
import com.moonface.wsee.models.CreateToilet
import com.moonface.wsee.models.Toilet
import com.moonface.wsee.models.ToiletQuery

interface IToiletRepository {
    suspend fun create(data: CreateToilet)

    suspend fun createReview(toiletId: String, data: CreateReview)

    suspend fun search(query: ToiletQuery): List<Toilet>
}