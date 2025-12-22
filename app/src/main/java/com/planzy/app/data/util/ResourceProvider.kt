package com.planzy.app.data.util

interface ResourceProvider {
    fun getString(resId: Int): String
}