package com.planzy.app.domain.service

import android.util.Log
import com.planzy.app.domain.model.Messages
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.repository.UserRepository
import kotlinx.coroutines.delay

class AuthService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    private val TAG = "AuthService"

    suspend fun registerUser(
        email: String,
        password: String,
        username: String
    ): Result<String> {
        return try {
            Log.d(TAG, "Registration started")

            val authResult = authRepository.signUp(email, password, username)
            if (authResult.isFailure) {
                return Result.failure(
                    Exception(authResult.exceptionOrNull()?.message ?: Messages.ERROR_REGISTRATION_FAILED)
                )
            }

            val authUser = authResult.getOrNull()!!
            Log.i(TAG, "Auth user created with ID: ${authUser.id}")

            delay(1000)

            val currentUser = authRepository.getCurrentUser()

            if (currentUser != null) {

                val userCreateResult = userRepository.createUserRecord(
                    authId = authUser.id,
                    email = email,
                    username = username
                )

                if (userCreateResult.isSuccess) {
                    Result.success(Messages.SUCCESS_REGISTRATION)
                } else {
                    Result.failure(Exception(Messages.ERROR_RECORD_DB_FAILED))
                }
            } else {
                Log.i(TAG, "Email verification enabled")
                Result.success(Messages.SUCCESS_VERIFICATION_EMAIL_SENT)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            Result.failure(Exception(e.message ?: Messages.ERROR_REGISTRATION_FAILED))
        }
    }

    suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
        return try {
            val result = userRepository.getUserByUsername(username)
            if (result.isSuccess) {
                Result.success(result.getOrNull() == null)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkEmailAvailability(email: String): Result<Boolean> {
        return try {
            val result = authRepository.checkEmailExistsInAuth(email)
            if (result.isSuccess) {
                Result.success(!(result.getOrNull() ?: false))
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(email: String): Result<String> {
        return try {
            val result = authRepository.resendVerificationEmail(email)
            if (result.isSuccess) {
                Result.success(Messages.SUCCESS_RESEND_VERIFICATION_EMAIL)
            } else {
                Result.failure(Exception(Messages.ERROR_VERIFICATION_EMAIL_RESEND))
            }
        } catch (e: Exception) {
            Result.failure(Exception(Messages.ERROR_VERIFICATION_EMAIL_RESEND))
        }
    }
}