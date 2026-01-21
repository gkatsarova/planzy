package com.planzy.app.domain.repository

import com.planzy.app.data.model.User

interface UserRepository {
    suspend fun createUserRecord(authId: String, email: String, username: String): Result<Unit>
    suspend fun getUserByUsername(username: String): Result<User?>
    suspend fun getUserByAuthId(authId: String): Result<User?>
}