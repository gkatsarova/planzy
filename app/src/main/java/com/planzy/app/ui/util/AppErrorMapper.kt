package com.planzy.app.ui.util

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException

fun AppError.toMessage(resourceProvider: ResourceProvider): String {
    return when (this) {
        AppError.USER_NOT_LOGGED_IN -> resourceProvider.getString(R.string.error_user_not_logged_in)
        AppError.VACATION_NOT_FOUND -> resourceProvider.getString(R.string.error_vacation_not_found)
        AppError.PLACE_ALREADY_IN_VACATION -> resourceProvider.getString(R.string.error_place_already_in_vacation)
        AppError.ERROR_LOADING_VACATIONS -> resourceProvider.getString(R.string.error_loading_vacations)
        AppError.ERROR_CREATING_VACATION -> resourceProvider.getString(R.string.error_creating_vacation)
        AppError.ERROR_EMPTY_VACATION_TITLE -> resourceProvider.getString(R.string.error_empty_vacation_title)
        AppError.ERROR_ADDING_PLACE_TO_VACATION -> resourceProvider.getString(R.string.error_adding_place_to_vacation)
        AppError.ERROR_REMOVING_PLACE -> resourceProvider.getString(R.string.error_removing_place)
        AppError.ERROR_LOADING_VACATION -> resourceProvider.getString(R.string.error_loading_vacation)
        AppError.ERROR_LOADING_PLACES -> resourceProvider.getString(R.string.error_loading_places)
        AppError.ERROR_PARSING_COMMENTS -> resourceProvider.getString(R.string.error_parsing_comments)
        AppError.ERROR_POSTING_COMMENT -> resourceProvider.getString(R.string.error_posting_comment)
        AppError.ERROR_UPDATING_COMMENT -> resourceProvider.getString(R.string.error_updating_comment)
        AppError.ERROR_DELETING_COMMENT -> resourceProvider.getString(R.string.error_deleting_comment)
        AppError.ERROR_LOADING_COMMUNITY_COMMENTS -> resourceProvider.getString(R.string.error_loading_community_comments)
        AppError.ERROR_SAVING_PLACE -> resourceProvider.getString(R.string.error_saving_place)
        AppError.ERROR_EMPTY_PLACE_ID -> resourceProvider.getString(R.string.empty_place_id)
        AppError.ERROR_USER_NOT_FOUND -> resourceProvider.getString(R.string.user_not_found)
        AppError.ERROR_LOADING_USER -> resourceProvider.getString(R.string.error_loading_user)
        AppError.ERROR_LOADING_FOLLOW_STATS -> resourceProvider.getString(R.string.error_loading_follow_stats)
        AppError.ERROR_LOADING_FOLLOWERS -> resourceProvider.getString(R.string.error_loading_followers)
        AppError.ERROR_LOADING_FOLLOWING -> resourceProvider.getString(R.string.error_loading_following)
        AppError.ERROR_UPDATING_FOLLOW_STATUS -> resourceProvider.getString(R.string.error_updating_follow_status)
        AppError.FAILED_TO_SAVE_VACATION -> resourceProvider.getString(R.string.failed_to_save_vacation)
        AppError.FAILED_TO_UNSAVE_VACATION -> resourceProvider.getString(R.string.failed_to_unsave_vacation)
        AppError.FAILED_TO_LOAD_SAVED_VACATIONS -> resourceProvider.getString(R.string.failed_to_load_saved_vacations)
        AppError.FAILED_TO_CHECK_SAVED_STATUS -> resourceProvider.getString(R.string.failed_to_check_saved_status)
        AppError.ERROR_DELETING_VACATION -> resourceProvider.getString(R.string.error_deleting_vacation)
        AppError.ERROR_VACATION_NOT_FOUND_OR_NO_PERMISSION -> resourceProvider.getString(R.string.error_vacation_not_found_or_no_permission)
        AppError.ERROR_LOGOUT_FAILED -> resourceProvider.getString(R.string.error_logout_failed)
        AppError.ERROR_DELETE_ACCOUNT_FAILED -> resourceProvider.getString(R.string.error_delete_account_failed)
        AppError.ERROR_LOADING_PROFILE_PICTURE -> resourceProvider.getString(R.string.error_loading_profile_picture)
        AppError.ERROR_PLACE_NOT_FOUND_OR_NO_PERMISSION -> resourceProvider.getString(R.string.unknown_error)
        AppError.UNKNOWN_ERROR -> resourceProvider.getString(R.string.unknown_error)
        AppError.EMPTY_COMMENT_TEXT -> resourceProvider.getString(R.string.empty_comment_text)
        AppError.RATING_ERROR -> resourceProvider.getString(R.string.rating_error)
        AppError.ERROR_INVALID_RESET_LINK -> resourceProvider.getString(R.string.error_invalid_reset_link)
        AppError.ERROR_SESSION -> resourceProvider.getString(R.string.error_session)
        AppError.ERROR_ACTIVE_SESSION -> resourceProvider.getString(R.string.error_active_session)
        AppError.ERROR_VERIFIED_USER_EMAIL -> resourceProvider.getString(R.string.error_verified_user_email)
        AppError.ERROR_RECORD_DB_FAILED -> resourceProvider.getString(R.string.error_record_db_failed)
        AppError.ERROR_EMAIL_VERIFICATION -> resourceProvider.getString(R.string.error_email_verification)
        AppError.ERROR_CREATING_AUTH_USER -> resourceProvider.getString(R.string.error_creating_auth_user)
        AppError.ERROR_LOGIN_FAILED -> resourceProvider.getString(R.string.error_login_failed)
        AppError.ERROR_SESSION_EXPIRED -> resourceProvider.getString(R.string.error_session_expired)
        AppError.ERROR_UPDATE_PASSWORD -> resourceProvider.getString(R.string.error_update_password)
        AppError.ERROR_INVALID_CREDENTIALS -> resourceProvider.getString(R.string.error_invalid_credentials)
        AppError.ERROR_NO_INTERNET -> resourceProvider.getString(R.string.error_no_internet)
        AppError.ERROR_EMAIL_NOT_VERIFIED -> resourceProvider.getString(R.string.error_email_not_verified)
        AppError.ERROR_EMAIL_EXISTS -> resourceProvider.getString(R.string.error_email_exists)
        AppError.ERROR_USERNAME_EXISTS -> resourceProvider.getString(R.string.error_username_exists)
        AppError.ERROR_REGISTRATION_FAILED -> resourceProvider.getString(R.string.error_registration_failed)
        AppError.ERROR_VERIFICATION_EMAIL_RESEND -> resourceProvider.getString(R.string.error_verification_email_resend)
        AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED -> resourceProvider.getString(R.string.error_password_reset_email_failed)
        AppError.ERROR_ANALYZING_INTENT -> resourceProvider.getString(R.string.error_analyzing)
        AppError.ERROR_CITY_NOT_FOUND -> resourceProvider.getString(R.string.error_city_not_found)
        AppError.ERROR_DESTINATION_DETAILS_FAILED -> resourceProvider.getString(R.string.where_is_your_dream_vacation)
        AppError.DESCRIBE_YOUR_DREAM_VACATION -> resourceProvider.getString(R.string.describe_your_dream_vacation)
        AppError.ERROR_API_LIMIT -> resourceProvider.getString(R.string.error_api_limit)
        AppError.ERROR_UNAUTHORIZED -> resourceProvider.getString(R.string.error_unauthorized)
        AppError.ERROR_NO_RESULTS_FOUND -> resourceProvider.getString(R.string.error_no_results_found)
    }
}

fun Throwable.toUserMessage(resourceProvider: ResourceProvider): String {
    return when (this) {
        is AppException -> this.error.toMessage(resourceProvider)
        else -> resourceProvider.getString(R.string.unknown_error)
    }
}