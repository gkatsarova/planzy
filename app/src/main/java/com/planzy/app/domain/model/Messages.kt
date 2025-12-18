package com.planzy.app.domain.model

object Messages {
    const val ERROR_PASSWORD_INVALID = "Password must be at least 8 characters including lowercase, uppercase, numbers and special symbols."
    const val ERROR_USERNAME_EXISTS = "This username already exists"
    const val ERROR_USERNAME_INVALID = "Username must be 3-20 symbols, including lowercase, numbers and special symbols(underscore, dot,)."
    const val ERROR_EMAIL_EXISTS = "This email is already registered. Please try logging in or use password reset."
    const val ERROR_EMAIL_INVALID = "Invalid email format."
    const val ERROR_REGISTRATION_FAILED = "Registration failed. Please try again."
    const val ERROR_RECORD_DB_FAILED = "Account created but profile setup failed."
    const val ERROR_VERIFICATION_EMAIL_RESEND = "Failed to resend email. Please try again."

    const val SUCCESS_REGISTRATION = "Registration successful! Welcome to Planzy."
    const val SUCCESS_VERIFICATION_EMAIL_SENT = "Verification email sent. Please check your inbox."
    const val SUCCESS_RESEND_VERIFICATION_EMAIL = "Verification email has been resent. Please check your inbox."
}