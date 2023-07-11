package com.moonface.wsee.models

data class CreateToilet(
    val cost: Cost? = null,
    val place: Place,
    val rating: Int,
)

data class CreateReview(
    val rating: Int
)
