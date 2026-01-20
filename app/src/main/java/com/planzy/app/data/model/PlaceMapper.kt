package com.planzy.app.data.model

import android.util.Log
import com.planzy.app.domain.model.ContactInfo
import com.planzy.app.domain.model.Location
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview

fun LocationDetailsResponse.toDomainModel(): Place {
    Log.d("PlaceMapper", " Mapping place: $name")
    Log.d("PlaceMapper", "  - Photo object: ${photo}")
    Log.d("PlaceMapper", "  - Photo images: ${photo?.images}")
    Log.d("PlaceMapper", "  - Large URL: ${photo?.images?.large?.url}")
    Log.d("PlaceMapper", "  - Medium URL: ${photo?.images?.medium?.url}")
    Log.d("PlaceMapper", "  - Original URL: ${photo?.images?.original?.url}")

    val photoUrl = photo?.images?.large?.url
        ?: photo?.images?.medium?.url
        ?: photo?.images?.original?.url

    Log.d("PlaceMapper", "  - Selected photoUrl: $photoUrl")

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
        photoUrl = photoUrl,
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

fun Place.toDTO(): PlaceDTO = PlaceDTO(
    id = null,
    locationId = id,
    name = name,
    address = location.address,
    latitude = location.latitude.toString(),
    longitude = location.longitude.toString(),
    rating = rating.toString(),
    description = description,
    imageUrl = photoUrl,
    category = category
)