package com.perpetuitylab.livenews

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object Welcome : NavKey

@Serializable data object Home : NavKey

@Serializable data object Settings : NavKey

@Serializable data class NetworkInspector(val channelId: Int, val pageUrl: String) : NavKey
