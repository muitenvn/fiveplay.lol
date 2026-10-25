package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FivePlayPreferences
import com.example.data.SettingsRepository
import com.example.model.ActionSlot
import com.example.model.ControlProfile
import com.example.model.MouseControlMode
import com.example.model.MovementStyle
import com.example.model.PcKeyCatalog
import com.example.model.PcKeySpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppMode {
    HUB,
    IN_GAME
}

data class RuntimeGameState(
    val appMode: AppMode = AppMode.HUB,
    val activeProfile: ControlProfile = ControlProfile.XBOX_GAMEPAD,
    val controlsVisible: Boolean = true,
    val isLandscapeLocked: Boolean = true,
    val hudExpanded: Boolean = true,
    val showQuickChatDialog: Boolean = false,
    val showMiniPcKeyboard: Boolean = false,
    val showKeybindModalForSlot: ActionSlot? = null,
    val pageLoadingProgress: Int = 0,
    val isPageLoading: Boolean = false,
    val currentFps: Int = 60,
    val hasGameCanvas: Boolean = false,
    val aimToggleLocked: Boolean = false,
    val sprintToggleLocked: Boolean = false,
    // Virtual PC Mouse Cursor normalized screen position (0.0 .. 1.0)
    val cursorNormX: Float = 0.50f,
    val cursorNormY: Float = 0.50f,
    val isCursorClicking: Boolean = false,
    val activePressedCodes: Set<String> = emptySet(),
    val lastInputLog: String = "Sẵn sàng: Tay Cầm Console (Xbox/PS5) & Con trỏ Chuột PC",
    val testerStickX: Float = 0f,
    val testerStickY: Float = 0f,
    val testerCameraYaw: Float = 0f,
    val testerCameraPitch: Float = 0f
)

data class FivePlayUiState(
    val prefs: FivePlayPreferences = FivePlayPreferences(),
    val runtime: RuntimeGameState = RuntimeGameState()
) {
    fun resolveKeySpec(slot: ActionSlot): PcKeySpec {
        val keyId = prefs.slotKeyBindings[slot] ?: slot.defaultKeyId
        return PcKeyCatalog.findById(keyId)
    }
}

class FivePlayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)
    private val runtimeState = MutableStateFlow(RuntimeGameState())
    val runtimeFlow: StateFlow<RuntimeGameState> = runtimeState.asStateFlow()

    val uiState: StateFlow<FivePlayUiState> = combine(
        repository.preferencesFlow.onStart { emit(FivePlayPreferences()) },
        runtimeState
    ) { prefs, runtime ->
        FivePlayUiState(prefs = prefs, runtime = runtime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FivePlayUiState()
    )

    fun launchGame(landscape: Boolean, customUrl: String? = null) {
        viewModelScope.launch {
            if (!customUrl.isNullOrBlank()) {
                repository.updateServerUrl(customUrl)
            }
            val currentPrefs = uiState.value.prefs
            runtimeState.update {
                it.copy(
                    appMode = AppMode.IN_GAME,
                    activeProfile = currentPrefs.defaultProfile,
                    isLandscapeLocked = landscape,
                    controlsVisible = currentPrefs.defaultProfile != ControlProfile.TOUCH_ONLY,
                    hudExpanded = false
                )
            }
        }
    }

    fun exitToHub() {
        runtimeState.update {
            it.copy(
                appMode = AppMode.HUB,
                isLandscapeLocked = false,
                aimToggleLocked = false,
                sprintToggleLocked = false,
                showMiniPcKeyboard = false,
                activePressedCodes = emptySet()
            )
        }
    }

    fun setActiveProfile(profile: ControlProfile) {
        runtimeState.update {
            it.copy(
                activeProfile = profile,
                controlsVisible = profile != ControlProfile.TOUCH_ONLY
            )
        }
    }

    fun cycleControlProfile() {
        val current = runtimeState.value.activeProfile
        val next = when (current) {
            ControlProfile.XBOX_GAMEPAD -> ControlProfile.ON_FOOT
            ControlProfile.ON_FOOT -> ControlProfile.DRIVING
            ControlProfile.DRIVING -> ControlProfile.XBOX_GAMEPAD
            ControlProfile.TOUCH_ONLY -> ControlProfile.XBOX_GAMEPAD
        }
        setActiveProfile(next)
    }

    fun toggleMouseControlMode() {
        val current = uiState.value.prefs.mouseControlMode
        val next = if (current == MouseControlMode.VIRTUAL_CURSOR) {
            MouseControlMode.CAMERA_3D
        } else {
            MouseControlMode.VIRTUAL_CURSOR
        }
        saveMouseControlMode(next)
        if (next == MouseControlMode.CAMERA_3D) {
            runtimeState.update { it.copy(cursorNormX = 0.5f, cursorNormY = 0.5f) }
        }
    }

    fun toggleMovementStyle() {
        val current = uiState.value.prefs.movementStyle
        val next = if (current == MovementStyle.ANALOG_STICK) {
            MovementStyle.WASD_BUTTONS
        } else {
            MovementStyle.ANALOG_STICK
        }
        saveMovementStyle(next)
    }

    fun toggleControlsVisibility() {
        runtimeState.update { it.copy(controlsVisible = !it.controlsVisible) }
    }

    fun toggleHudExpanded() {
        runtimeState.update { it.copy(hudExpanded = !it.hudExpanded) }
    }

    fun toggleMiniPcKeyboard() {
        runtimeState.update { it.copy(showMiniPcKeyboard = !it.showMiniPcKeyboard) }
    }

    fun toggleOrientationLock() {
        runtimeState.update { it.copy(isLandscapeLocked = !it.isLandscapeLocked) }
    }

    fun setShowQuickChat(show: Boolean) {
        runtimeState.update { it.copy(showQuickChatDialog = show) }
    }

    fun openKeybindModal(slot: ActionSlot?) {
        runtimeState.update { it.copy(showKeybindModalForSlot = slot) }
    }

    fun toggleAimLock(): Boolean {
        val next = !runtimeState.value.aimToggleLocked
        runtimeState.update { it.copy(aimToggleLocked = next) }
        return next
    }

    fun toggleSprintLock(): Boolean {
        val next = !runtimeState.value.sprintToggleLocked
        runtimeState.update { it.copy(sprintToggleLocked = next) }
        return next
    }

    fun onPageLoadProgress(progress: Int, isLoading: Boolean) {
        runtimeState.update {
            it.copy(pageLoadingProgress = progress, isPageLoading = isLoading)
        }
    }

    fun onTelemetryUpdate(fps: Int, hasCanvas: Boolean) {
        runtimeState.update {
            it.copy(currentFps = fps, hasGameCanvas = hasCanvas)
        }
    }

    fun recordInputEvent(spec: PcKeySpec, isDown: Boolean) {
        runtimeState.update { state ->
            val updatedSet = if (isDown) {
                state.activePressedCodes + spec.code
            } else {
                state.activePressedCodes - spec.code
            }
            val actionLabel = if (isDown) "NHẤN" else "THẢ"
            state.copy(
                activePressedCodes = updatedSet,
                isCursorClicking = if (spec.isMouse) isDown else state.isCursorClicking,
                lastInputLog = "$actionLabel: ${spec.label} [${spec.shortText}]"
            )
        }
    }

    fun setCursorClicking(clicking: Boolean) {
        runtimeState.update { it.copy(isCursorClicking = clicking) }
    }

    fun moveVirtualCursorByDelta(
        dxPx: Float,
        dyPx: Float,
        viewportWidthPx: Float,
        viewportHeightPx: Float
    ): Pair<Float, Float> {
        val w = viewportWidthPx.coerceAtLeast(1f)
        val h = viewportHeightPx.coerceAtLeast(1f)
        val current = runtimeState.value
        val nextX = (current.cursorNormX + (dxPx / w)).coerceIn(0.01f, 0.99f)
        val nextY = (current.cursorNormY + (dyPx / h)).coerceIn(0.01f, 0.99f)
        val newYaw = (current.testerCameraYaw + dxPx * 0.35f) % 360f
        val newPitch = (current.testerCameraPitch + dyPx * 0.35f).coerceIn(-85f, 85f)
        runtimeState.update {
            it.copy(
                cursorNormX = nextX,
                cursorNormY = nextY,
                testerCameraYaw = newYaw,
                testerCameraPitch = newPitch,
                lastInputLog = "Chuột PC: (${(nextX * 100).toInt()}%, ${(nextY * 100).toInt()}%) | Δ(${dxPx.toInt()}, ${dyPx.toInt()})"
            )
        }
        return nextX to nextY
    }

    fun updateTesterStick(x: Float, y: Float) {
        runtimeState.update { state ->
            state.copy(
                testerStickX = x,
                testerStickY = y,
                lastInputLog = if (x == 0f && y == 0f) {
                    "Thả cần Analog trái (WASD nghỉ)"
                } else {
                    "Analog Trái: X=${"%.2f".format(x)}, Y=${"%.2f".format(y)}"
                }
            )
        }
    }

    fun updateTesterCamera(dx: Float, dy: Float) {
        runtimeState.update { state ->
            val newYaw = (state.testerCameraYaw + dx * 0.4f) % 360f
            val newPitch = (state.testerCameraPitch + dy * 0.4f).coerceIn(-85f, 85f)
            state.copy(
                testerCameraYaw = newYaw,
                testerCameraPitch = newPitch,
                lastInputLog = "Xoay Chuột 3D: ΔX=${dx.toInt()}, ΔY=${dy.toInt()}"
            )
        }
    }

    // Settings mutations
    fun saveServerUrl(url: String) = viewModelScope.launch { repository.updateServerUrl(url) }
    fun saveDefaultProfile(profile: ControlProfile) = viewModelScope.launch {
        repository.updateDefaultProfile(profile)
        setActiveProfile(profile)
    }
    fun saveMouseControlMode(mode: MouseControlMode) = viewModelScope.launch {
        repository.updateMouseControlMode(mode)
    }
    fun saveMovementStyle(style: MovementStyle) = viewModelScope.launch {
        repository.updateMovementStyle(style)
    }
    fun saveControlOpacity(opacity: Float) = viewModelScope.launch { repository.updateControlOpacity(opacity) }
    fun saveControlScale(scale: Float) = viewModelScope.launch { repository.updateControlScale(scale) }
    fun saveMouseSensitivity(sens: Float) = viewModelScope.launch { repository.updateMouseSensitivity(sens) }
    fun saveInvertYAxis(invert: Boolean) = viewModelScope.launch { repository.updateInvertYAxis(invert) }
    fun saveHapticFeedback(enabled: Boolean) = viewModelScope.launch { repository.updateHapticFeedback(enabled) }
    fun saveDesktopUserAgent(enabled: Boolean) = viewModelScope.launch { repository.updateDesktopUserAgent(enabled) }
    fun saveWebGlFpsBoost(enabled: Boolean) = viewModelScope.launch { repository.updateWebGlFpsBoost(enabled) }
    fun saveAutoSprintOnEdge(enabled: Boolean) = viewModelScope.launch { repository.updateAutoSprintOnEdge(enabled) }
    fun saveShowQuickTopBar(enabled: Boolean) = viewModelScope.launch { repository.updateShowQuickTopBar(enabled) }
    fun saveShowPhoneArrows(enabled: Boolean) = viewModelScope.launch { repository.updateShowPhoneArrows(enabled) }
    fun saveSlotBinding(slot: ActionSlot, keyId: String) = viewModelScope.launch {
        repository.updateSlotBinding(slot, keyId)
        runtimeState.update { it.copy(showKeybindModalForSlot = null) }
    }
    fun resetAllBindings() = viewModelScope.launch { repository.resetAllBindings() }
}
