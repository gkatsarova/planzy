package com.planzy.app.domain.repository

import com.planzy.app.data.model.User
import java.io.File

interface UserRepository {
    suspend fun createUserRecord(authId: String, email: String, username: String): Result<Unit>
    suspend fun getUserByUsername(username: String): Result<User?>
    suspend fun getUserByAuthId(authId: String): Result<User?>
    suspend fun uploadProfilePicture(imageFile: File): Result<String>
    suspend fun updateProfilePictureUrl(url: String): Result<Unit>
    suspend fun deleteProfilePicture(pictureUrl: String): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<User>>
}