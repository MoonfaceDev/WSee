package com.moonface.wsee.toilet_repository

import com.moonface.wsee.models.Toilet
import com.moonface.wsee.models.ToiletQuery

interface IToiletRepository {
    suspend fun search(query: ToiletQuery): List<Toilet>
}