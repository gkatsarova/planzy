package com.planzy.app.data.model

import com.planzy.app.domain.model.ContactInfo
import com.planzy.app.domain.model.Location
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview

fun LocationDetailsResponse.toDomainModel(): Place {
    return Place(
        id = locationId,
        name = name,
        location = Location(
            latitude = latitude?.toDoubleOrNull() ?: 0.0,
            longitude = longitude?.toDoubleOrNull() ?: 0.0,
            address = addressObj?.addressString ?: ""
        ),
        rating = rating ?: 0.0,
        reviewsCount = numReviews ?: 0,
        description = description,
        photoUrl = photo?.images?.large?.url,
        category = category?.localizedName,
        contact = ContactInfo(
            phone = phone,
            website = website
        )
    )
}

fun ReviewData.toDomainModel(): PlaceReview {
    return PlaceReview(
        id = id,
        author = user?.username ?: "Anonymous",
        rating = rating ?: 0,
        text = text,
        date = publishedDate ?: ""
    )
}