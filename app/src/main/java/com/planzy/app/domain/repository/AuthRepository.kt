package com.planzy.app.domain.repository

import io.github.jan.supabase.auth.user.UserInfo

interface AuthRepository {
    suspend fun signUp(email: String, password: String, username: String): Result<UserInfo>
    suspend fun signIn(email: String, password: String): Result<UserInfo>
    suspend fun checkEmailExistsInAuth(email: String): Result<Boolean>
    suspend fun checkUsernameExists(username: String): Result<Boolean>
    suspend fun resendVerificationEmail(email: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun getCurrentUser(): UserInfo?
}