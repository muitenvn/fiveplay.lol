package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ActionSlot
import com.example.model.ControlProfile
import com.example.model.MouseControlMode
import com.example.model.MovementStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fiveplay_settings")

data class FivePlayPreferences(
    val serverUrl: String = "https://fiveplay.lol",
    val defaultProfile: ControlProfile = ControlProfile.ON_FOOT,
    val mouseControlMode: MouseControlMode = MouseControlMode.VIRTUAL_CURSOR,
    val movementStyle: MovementStyle = MovementStyle.ANALOG_STICK,
    val controlOpacity: Float = 0.80f,
    val controlScale: Float = 1.0f,
    val mouseSensitivity: Float = 1.65f,
    val invertYAxis: Boolean = false,
    val hapticFeedback: Boolean = true,
    val desktopUserAgent: Boolean = true,
    val webGlFpsBoost: Boolean = true,
    val autoSprintOnEdge: Boolean = true,
    val showQuickTopBar: Boolean = true,
    val showPhoneArrows: Boolean = true,
    val slotKeyBindings: Map<ActionSlot, String> = ActionSlot.entries.associateWith { it.defaultKeyId }
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val DEFAULT_PROFILE = stringPreferencesKey("default_profile")
        val MOUSE_MODE = stringPreferencesKey("mouse_control_mode")
        val MOVEMENT_STYLE = stringPreferencesKey("movement_style")
        val CONTROL_OPACITY = floatPreferencesKey("control_opacity")
        val CONTROL_SCALE = floatPreferencesKey("control_scale")
        val MOUSE_SENSITIVITY = floatPreferencesKey("mouse_sensitivity")
        val INVERT_Y_AXIS = booleanPreferencesKey("invert_y_axis")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val DESKTOP_USER_AGENT = booleanPreferencesKey("desktop_user_agent")
        val WEBGL_FPS_BOOST = booleanPreferencesKey("webgl_fps_boost")
        val AUTO_SPRINT_ON_EDGE = booleanPreferencesKey("auto_sprint_on_edge")
        val SHOW_QUICK_TOP_BAR = booleanPreferencesKey("show_quick_top_bar")
        val SHOW_PHONE_ARROWS = booleanPreferencesKey("show_phone_arrows")
        fun slotKey(slot: ActionSlot) = stringPreferencesKey("bind_${slot.slotId}")
    }

    val preferencesFlow: Flow<FivePlayPreferences> = context.dataStore.data.map { prefs ->
        val profileName = prefs[Keys.DEFAULT_PROFILE] ?: ControlProfile.ON_FOOT.name
        val profile = runCatching { ControlProfile.valueOf(profileName) }.getOrDefault(ControlProfile.ON_FOOT)

        val mouseModeName = prefs[Keys.MOUSE_MODE] ?: MouseControlMode.VIRTUAL_CURSOR.name
        val mouseMode = runCatching { MouseControlMode.valueOf(mouseModeName) }.getOrDefault(MouseControlMode.VIRTUAL_CURSOR)

        val moveStyleName = prefs[Keys.MOVEMENT_STYLE] ?: MovementStyle.ANALOG_STICK.name
        val moveStyle = runCatching { MovementStyle.valueOf(moveStyleName) }.getOrDefault(MovementStyle.ANALOG_STICK)

        val bindings = ActionSlot.entries.associateWith { slot ->
            prefs[Keys.slotKey(slot)] ?: slot.defaultKeyId
        }

        FivePlayPreferences(
            serverUrl = prefs[Keys.SERVER_URL] ?: "https://fiveplay.lol",
            defaultProfile = profile,
            mouseControlMode = mouseMode,
            movementStyle = moveStyle,
            controlOpacity = prefs[Keys.CONTROL_OPACITY] ?: 0.80f,
            controlScale = prefs[Keys.CONTROL_SCALE] ?: 1.0f,
            mouseSensitivity = prefs[Keys.MOUSE_SENSITIVITY] ?: 1.65f,
            invertYAxis = prefs[Keys.INVERT_Y_AXIS] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            desktopUserAgent = prefs[Keys.DESKTOP_USER_AGENT] ?: true,
            webGlFpsBoost = prefs[Keys.WEBGL_FPS_BOOST] ?: true,
            autoSprintOnEdge = prefs[Keys.AUTO_SPRINT_ON_EDGE] ?: true,
            showQuickTopBar = prefs[Keys.SHOW_QUICK_TOP_BAR] ?: true,
            showPhoneArrows = prefs[Keys.SHOW_PHONE_ARROWS] ?: true,
            slotKeyBindings = bindings
        )
    }

    suspend fun updateServerUrl(url: String) {
        val formatted = if (url.startsWith("http://") || url.startsWith("https://")) {
            url.trim()
        } else {
            "https://${url.trim()}"
        }
        context.dataStore.edit { it[Keys.SERVER_URL] = formatted }
    }

    suspend fun updateDefaultProfile(profile: ControlProfile) {
        context.dataStore.edit { it[Keys.DEFAULT_PROFILE] = profile.name }
    }

    suspend fun updateMouseControlMode(mode: MouseControlMode) {
        context.dataStore.edit { it[Keys.MOUSE_MODE] = mode.name }
    }

    suspend fun updateMovementStyle(style: MovementStyle) {
        context.dataStore.edit { it[Keys.MOVEMENT_STYLE] = style.name }
    }

    suspend fun updateControlOpacity(opacity: Float) {
        context.dataStore.edit { it[Keys.CONTROL_OPACITY] = opacity.coerceIn(0.25f, 1.0f) }
    }

    suspend fun updateControlScale(scale: Float) {
        context.dataStore.edit { it[Keys.CONTROL_SCALE] = scale.coerceIn(0.75f, 1.35f) }
    }

    suspend fun updateMouseSensitivity(sensitivity: Float) {
        context.dataStore.edit { it[Keys.MOUSE_SENSITIVITY] = sensitivity.coerceIn(0.4f, 4.0f) }
    }

    suspend fun updateInvertYAxis(invert: Boolean) {
        context.dataStore.edit { it[Keys.INVERT_Y_AXIS] = invert }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun updateDesktopUserAgent(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DESKTOP_USER_AGENT] = enabled }
    }

    suspend fun updateWebGlFpsBoost(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WEBGL_FPS_BOOST] = enabled }
    }

    suspend fun updateAutoSprintOnEdge(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SPRINT_ON_EDGE] = enabled }
    }

    suspend fun updateShowQuickTopBar(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_QUICK_TOP_BAR] = enabled }
    }

    suspend fun updateShowPhoneArrows(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_PHONE_ARROWS] = enabled }
    }

    suspend fun updateSlotBinding(slot: ActionSlot, keyId: String) {
        context.dataStore.edit { it[Keys.slotKey(slot)] = keyId }
    }

    suspend fun resetAllBindings() {
        context.dataStore.edit { prefs ->
            ActionSlot.entries.forEach { slot ->
                prefs[Keys.slotKey(slot)] = slot.defaultKeyId
            }
            prefs[Keys.CONTROL_OPACITY] = 0.80f
            prefs[Keys.CONTROL_SCALE] = 1.0f
            prefs[Keys.MOUSE_SENSITIVITY] = 1.65f
        }
    }
}
