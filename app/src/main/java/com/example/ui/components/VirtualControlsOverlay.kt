package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionSlot
import com.example.model.ControlProfile
import com.example.model.MouseControlMode
import com.example.model.MovementStyle
import com.example.model.PcKeyCatalog
import com.example.model.PcKeySpec
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HudGlass
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ViceMagenta
import com.example.viewmodel.FivePlayUiState

@Composable
fun VirtualControlsOverlay(
    uiState: FivePlayUiState,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float, Float, Float) -> Unit,
    onCursorTapClick: () -> Unit,
    onWheelScroll: (Int) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleMoveStyle: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleHud: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onToggleOrientation: () -> Unit,
    onOpenQuickChat: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs
    val runtime = uiState.runtime
    val scale = prefs.controlScale
    val opacity = prefs.controlOpacity
    val haptics = prefs.hapticFeedback

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 1. Virtual Mouse & 3D Camera Trackpad Surface
        // In TOUCH_ONLY mode, covers full screen; otherwise covers the right 62% of screen
        Row(modifier = Modifier.fillMaxSize()) {
            if (runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY) {
                Spacer(modifier = Modifier.weight(0.38f))
            }
            MouseLookTouchpad(
                sensitivity = prefs.mouseSensitivity,
                invertY = prefs.invertYAxis,
                hapticsEnabled = haptics,
                onMouseDelta = onMouseDelta,
                onTapClick = onCursorTapClick,
                modifier = Modifier
                    .weight(if (runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY) 0.62f else 1f)
                    .fillMaxHeight()
            )
        }

        // 2. Crisp Virtual PC Mouse Pointer Arrow & 3D Crosshair Layer (replaces giant touch blob!)
        VirtualMouseCursorOverlay(
            cursorNormX = runtime.cursorNormX,
            cursorNormY = runtime.cursorNormY,
            isClicking = runtime.isCursorClicking,
            mouseMode = prefs.mouseControlMode,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Profile-specific controls layer
        AnimatedVisibility(
            visible = runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            when (runtime.activeProfile) {
                ControlProfile.ON_FOOT -> OnFootControlsLayout(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = { dx, dy -> onMouseDelta(dx, dy, 1000f, 600f) },
                    onWheelScroll = onWheelScroll,
                    onStickVector = onStickVector
                )

                ControlProfile.DRIVING -> DrivingControlsLayout(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged
                )

                ControlProfile.XBOX_GAMEPAD -> XboxGamepadLayout(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = { dx, dy -> onMouseDelta(dx, dy, 1000f, 600f) },
                    onStickVector = onStickVector
                )

                ControlProfile.TOUCH_ONLY -> Unit
            }
        }

        // 4. Top Floating Cyber-HUD Bar + Quick PC Key Strip (1-5, F1-F8, ESC, TAB, I, M, P, T)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FloatingCyberHud(
                uiState = uiState,
                onCycleProfile = onCycleProfile,
                onToggleMouseMode = onToggleMouseMode,
                onToggleMoveStyle = onToggleMoveStyle,
                onToggleControls = onToggleControls,
                onToggleHud = onToggleHud,
                onToggleMiniPcKeyboard = onToggleMiniPcKeyboard,
                onToggleOrientation = onToggleOrientation,
                onOpenQuickChat = onOpenQuickChat,
                onReloadPage = onReloadPage,
                onExitToHub = onExitToHub
            )

            if (runtime.controlsVisible && prefs.showQuickTopBar) {
                QuickTopKeyStrip(
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
            }
        }

        // 5. Full On-Screen Mini PC Keyboard Overlay (when toggled)
        AnimatedVisibility(
            visible = runtime.showMiniPcKeyboard,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            MiniPcKeyboardOverlay(
                haptics = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onClose = onToggleMiniPcKeyboard
            )
        }
    }
}

@Composable
private fun FloatingCyberHud(
    uiState: FivePlayUiState,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleMoveStyle: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleHud: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onToggleOrientation: () -> Unit,
    onOpenQuickChat: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runtime = uiState.runtime
    val prefs = uiState.prefs

    Surface(
        modifier = modifier.testTag("floating_cyber_hud"),
        color = HudGlass,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Live FPS Badge + Expand/Collapse HUD
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .clickable(onClick = onToggleHud)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("hud_toggle_expand"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (runtime.currentFps >= 45) NeonEmerald else LosSantosGold)
                )
                Text(
                    text = "${runtime.currentFps} FPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonEmerald,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Thu/Mở HUD",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
            }

            // Virtual PC Mouse Mode Toggle (CHUỘT PC vs TÂM 3D)
            HudChipButton(
                icon = if (prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR) {
                    Icons.Default.Mouse
                } else {
                    Icons.Default.AdsClick
                },
                label = prefs.mouseControlMode.badgeVi,
                accent = NeonEmerald,
                onClick = onToggleMouseMode,
                testTag = "hud_mouse_mode_btn"
            )

            // Quick Profile Switch (Đi Bộ / Lái Xe / Xbox)
            HudChipButton(
                icon = when (runtime.activeProfile) {
                    ControlProfile.ON_FOOT -> Icons.AutoMirrored.Filled.DirectionsRun
                    ControlProfile.DRIVING -> Icons.Default.DirectionsCar
                    ControlProfile.XBOX_GAMEPAD -> Icons.Default.Gamepad
                    ControlProfile.TOUCH_ONLY -> Icons.Default.VisibilityOff
                },
                label = when (runtime.activeProfile) {
                    ControlProfile.ON_FOOT -> "Đi Bộ"
                    ControlProfile.DRIVING -> "Lái Xe"
                    ControlProfile.XBOX_GAMEPAD -> "Xbox"
                    ControlProfile.TOUCH_ONLY -> "Chỉ Chuột"
                },
                accent = LosSantosGold,
                onClick = onCycleProfile,
                testTag = "hud_cycle_profile_btn"
            )

            // Toggle full on-screen PC Keyboard (QWERTY + F1-F8)
            HudChipButton(
                icon = Icons.Default.Keyboard,
                label = "Phím PC",
                accent = if (runtime.showMiniPcKeyboard) ViceMagenta else ElectricBlue,
                onClick = onToggleMiniPcKeyboard,
                testTag = "hud_pc_keyboard_btn"
            )

            // Show/Hide virtual keys
            HudIconAction(
                icon = if (runtime.controlsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDesc = "Ẩn/Hiện Phím",
                tint = if (runtime.controlsVisible) NeonEmerald else Color.White,
                onClick = onToggleControls,
                testTag = "hud_visibility_btn"
            )

            if (runtime.hudExpanded) {
                HudChipButton(
                    icon = Icons.Default.Gamepad,
                    label = if (prefs.movementStyle == MovementStyle.ANALOG_STICK) "Analog" else "WASD",
                    accent = ElectricBlue,
                    onClick = onToggleMoveStyle,
                    testTag = "hud_move_style_btn"
                )
                HudIconAction(
                    icon = Icons.Default.ChatBubbleOutline,
                    contentDesc = "Gõ Chat (T)",
                    tint = LosSantosGold,
                    onClick = onOpenQuickChat,
                    testTag = "hud_chat_btn"
                )
                HudIconAction(
                    icon = Icons.Default.ScreenRotation,
                    contentDesc = "Xoay Ngang/Dọc",
                    tint = LosSantosGold,
                    onClick = onToggleOrientation,
                    testTag = "hud_rotate_btn"
                )
                HudIconAction(
                    icon = Icons.Default.Refresh,
                    contentDesc = "Tải lại game",
                    tint = Color.White,
                    onClick = onReloadPage,
                    testTag = "hud_reload_btn"
                )
                HudIconAction(
                    icon = Icons.Default.Settings,
                    contentDesc = "Menu Cài Đặt",
                    tint = ViceMagenta,
                    onClick = onExitToHub,
                    testTag = "hud_settings_hub_btn"
                )
            }
        }
    }
}

@Composable
private fun HudChipButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HudIconAction(
    icon: ImageVector,
    contentDesc: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun OnFootControlsLayout(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float) -> Unit,
    onWheelScroll: (Int) -> Unit,
    onStickVector: (Float, Float) -> Unit
) {
    val prefs = uiState.prefs
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // 1. Mid-LeftGTA Phone & Menu Navigation D-Pad (↑ ↓ ← → + ENTER + BACKSPACE)
        if (prefs.showPhoneArrows) {
            PhoneArrowCluster(
                scale = scale,
                opacity = opacity,
                haptics = haptics,
                onKeyStateChanged = onKeyStateChanged,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(bottom = 64.dp)
            )
        }

        // 2. Bottom-Left Movement + Modifiers (SHIFT, CTRL, ALT)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (prefs.movementStyle == MovementStyle.ANALOG_STICK) {
                VirtualAnalogStick(
                    sizeDp = (134 * scale).dp,
                    opacity = opacity,
                    autoSprint = prefs.autoSprintOnEdge,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    onVectorChanged = onStickVector
                )
            } else {
                WasdKeyCluster(
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged
                )
            }

            // Dedicated Left-Thumb Modifier Column: SHIFT (Sprint), CTRL (Crouch), ALT (Third-Eye), N (Mic)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TacticalKeyButton(
                    label = "ALT",
                    subLabel = "Mắt thần",
                    keySpec = uiState.resolveKeySpec(ActionSlot.ALT_EYE),
                    sizeDp = (44 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_alt"
                )
                TacticalKeyButton(
                    label = "CTRL",
                    subLabel = "Ngồi",
                    keySpec = uiState.resolveKeySpec(ActionSlot.CROUCH),
                    sizeDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_ctrl"
                )
                TacticalKeyButton(
                    label = "SHIFT",
                    subLabel = "Chạy",
                    keySpec = uiState.resolveKeySpec(ActionSlot.SPRINT),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_shift"
                )
            }
        }

        // 3. Mid-Right Tactical Row (Scroll Wheel Up/Down, Q Cover, E Interact, G Seatbelt/Grenade, X HandsUp, C, V, N)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 78.dp, end = 4.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Scroll wheel + Camera V + Mic N
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniSquareKeyButton(
                    label = "LĂN ↑",
                    keySpec = PcKeyCatalog.WHEEL_UP,
                    sizeDp = (36 * scale).dp,
                    widthDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = { spec, down ->
                        onKeyStateChanged(spec, down)
                        if (down) onWheelScroll(-120)
                    },
                    testTagName = "btn_wheel_up"
                )
                MiniSquareKeyButton(
                    label = "LĂN ↓",
                    keySpec = PcKeyCatalog.WHEEL_DOWN,
                    sizeDp = (36 * scale).dp,
                    widthDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = { spec, down ->
                        onKeyStateChanged(spec, down)
                        if (down) onWheelScroll(120)
                    },
                    testTagName = "btn_wheel_down"
                )
                MiniSquareKeyButton(
                    label = "CAM V",
                    keySpec = uiState.resolveKeySpec(ActionSlot.CAMERA),
                    sizeDp = (36 * scale).dp,
                    widthDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_cam"
                )
                MiniSquareKeyButton(
                    label = "MIC N",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VOICE_N),
                    sizeDp = (36 * scale).dp,
                    widthDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_mic"
                )
            }

            // Secondary Action Row: X (Hands Up), G (Grenade/Belt), C (Look back), Q (Cover)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TacticalKeyButton(
                    label = "X",
                    subLabel = "Giơ tay",
                    keySpec = uiState.resolveKeySpec(ActionSlot.HANDS_UP),
                    sizeDp = (44 * scale).dp,
                    opacity = opacity,
                    accentColor = ViceMagenta,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_x"
                )
                TacticalKeyButton(
                    label = "G",
                    subLabel = "Lựu/Đai",
                    keySpec = uiState.resolveKeySpec(ActionSlot.GRENADE),
                    sizeDp = (44 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_g"
                )
                TacticalKeyButton(
                    label = "C",
                    subLabel = "Nhìn sau",
                    keySpec = uiState.resolveKeySpec(ActionSlot.LOOK_BACK),
                    sizeDp = (44 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_c"
                )
                TacticalKeyButton(
                    label = "Q",
                    subLabel = "Ẩn nấp",
                    keySpec = uiState.resolveKeySpec(ActionSlot.COVER),
                    sizeDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_cover"
                )
            }
        }

        // 4. Bottom-Right Primary Combat, Mouse Click & Vehicle Cluster
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upper Arc: E (Interact), R (Reload), F (Enter/Exit Vehicle), RMB (Aim / Right Click)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalKeyButton(
                    label = "E",
                    subLabel = "Dùng/Còi",
                    keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_interact"
                )
                TacticalKeyButton(
                    label = "R",
                    subLabel = "Nạp/Đánh",
                    keySpec = uiState.resolveKeySpec(ActionSlot.RELOAD),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_reload"
                )
                TacticalKeyButton(
                    label = "VÀO XE",
                    subLabel = "Phím F",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                    sizeDp = (54 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_vehicle"
                )
                TacticalKeyButton(
                    label = "NGẮM",
                    subLabel = "Chuột Phải",
                    keySpec = uiState.resolveKeySpec(ActionSlot.AIM),
                    sizeDp = (58 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    allowDragAim = true,
                    sensitivity = prefs.mouseSensitivity,
                    invertY = prefs.invertYAxis,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = onMouseDelta,
                    testTagName = "btn_onfoot_aim"
                )
            }

            // Bottom Row: TAB (Weapon Wheel), SPACE (Jump), BẮN / CLICK TRÁI (LMB)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TacticalKeyButton(
                    label = "TAB",
                    subLabel = "Kho Súng",
                    keySpec = uiState.resolveKeySpec(ActionSlot.WEAPON),
                    sizeDp = (52 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_weapon"
                )
                TacticalKeyButton(
                    label = "NHẢY",
                    subLabel = "Space",
                    keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                    sizeDp = (58 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_jump"
                )
                TacticalKeyButton(
                    label = "BẮN / CLICK",
                    subLabel = "Chuột Trái",
                    keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                    sizeDp = (76 * scale).dp,
                    opacity = opacity,
                    accentColor = DangerRed,
                    hapticsEnabled = haptics,
                    allowDragAim = true,
                    sensitivity = prefs.mouseSensitivity,
                    invertY = prefs.invertYAxis,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = onMouseDelta,
                    testTagName = "btn_onfoot_fire"
                )
            }
        }
    }
}

@Composable
private fun DrivingControlsLayout(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Left-Center Driving Accessories (Exit Car F, Seatbelt G, Horn E, Lights H, Lock L, Camera V, Look Back C)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacticalKeyButton(
                    label = "RA XE",
                    subLabel = "Phím F",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_exit"
                )
                TacticalKeyButton(
                    label = "DÂY ĐAI",
                    subLabel = "Phím G",
                    keySpec = PcKeyCatalog.KEY_G,
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_belt"
                )
                TacticalKeyButton(
                    label = "KHÓA XE",
                    subLabel = "Phím L",
                    keySpec = PcKeyCatalog.KEY_L,
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_lock"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacticalKeyButton(
                    label = "CÒI XE",
                    subLabel = "Phím E",
                    keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_horn"
                )
                TacticalKeyButton(
                    label = "ĐÈN XE",
                    subLabel = "Phím H",
                    keySpec = PcKeyCatalog.KEY_H,
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_lights"
                )
                TacticalKeyButton(
                    label = "GÓC CAM",
                    subLabel = "Phím V",
                    keySpec = uiState.resolveKeySpec(ActionSlot.CAMERA),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = ViceMagenta,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_cam"
                )
            }
        }

        // Bottom-Left Steering Left (A) & Right (D)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PedalButton(
                label = "◄ TRÁI",
                subLabel = "Lái A",
                keySpec = PcKeyCatalog.KEY_A,
                widthDp = (80 * scale).dp,
                heightDp = (74 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_left"
            )
            PedalButton(
                label = "PHẢI ►",
                subLabel = "Lái D",
                keySpec = PcKeyCatalog.KEY_D,
                widthDp = (80 * scale).dp,
                heightDp = (74 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_right"
            )
        }

        // Bottom-Right Pedals: Nitro (Shift), Drift/Handbrake (Space), Brake/Reverse (S), Gas (W)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 6.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TacticalKeyButton(
                    label = "NITRO",
                    subLabel = "Shift",
                    keySpec = PcKeyCatalog.KEY_SHIFT,
                    sizeDp = (54 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_nitro"
                )
                TacticalKeyButton(
                    label = "DRIFT",
                    subLabel = "Phanh Tay",
                    keySpec = PcKeyCatalog.KEY_SPACE,
                    sizeDp = (60 * scale).dp,
                    opacity = opacity,
                    accentColor = ViceMagenta,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_handbrake"
                )
            }
            PedalButton(
                label = "PHANH",
                subLabel = "Lùi (S)",
                keySpec = PcKeyCatalog.KEY_S,
                widthDp = (66 * scale).dp,
                heightDp = (90 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_brake"
            )
            PedalButton(
                label = "GA",
                subLabel = "Tăng tốc (W)",
                keySpec = PcKeyCatalog.KEY_W,
                widthDp = (72 * scale).dp,
                heightDp = (124 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_gas"
            )
        }
    }
}

@Composable
private fun XboxGamepadLayout(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float) -> Unit,
    onStickVector: (Float, Float) -> Unit
) {
    val prefs = uiState.prefs
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Left Triggers LB / LT (placed below top strip)
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TacticalKeyButton(
                label = "LT",
                subLabel = "Ngắm",
                keySpec = PcKeyCatalog.MOUSE_RIGHT,
                sizeDp = (54 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_lt"
            )
            TacticalKeyButton(
                label = "LB",
                subLabel = "Kho Súng",
                keySpec = PcKeyCatalog.KEY_TAB,
                sizeDp = (50 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_lb"
            )
        }

        // Right Triggers RB / RT
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TacticalKeyButton(
                label = "RB",
                subLabel = "Ẩn Nấp",
                keySpec = PcKeyCatalog.KEY_Q,
                sizeDp = (50 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_rb"
            )
            TacticalKeyButton(
                label = "RT",
                subLabel = "Bắn / Click",
                keySpec = PcKeyCatalog.MOUSE_LEFT,
                sizeDp = (56 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                allowDragAim = true,
                sensitivity = prefs.mouseSensitivity,
                invertY = prefs.invertYAxis,
                onKeyStateChanged = onKeyStateChanged,
                onMouseDelta = onMouseDelta,
                testTagName = "btn_xbox_rt"
            )
        }

        // Left Analog Stick + D-Pad
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VirtualAnalogStick(
                sizeDp = (132 * scale).dp,
                opacity = opacity,
                autoSprint = prefs.autoSprintOnEdge,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onVectorChanged = onStickVector
            )
            PhoneArrowCluster(
                scale = scale,
                opacity = opacity,
                haptics = haptics,
                onKeyStateChanged = onKeyStateChanged
            )
        }

        // Right ABXY Diamond Cluster
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size((164 * scale).dp)
        ) {
            TacticalKeyButton(
                label = "Y",
                subLabel = "Vào Xe (F)",
                keySpec = PcKeyCatalog.KEY_F,
                sizeDp = (52 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_y",
                modifier = Modifier.align(Alignment.TopCenter)
            )
            TacticalKeyButton(
                label = "X",
                subLabel = "Nạp (R)",
                keySpec = PcKeyCatalog.KEY_R,
                sizeDp = (52 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_x",
                modifier = Modifier.align(Alignment.CenterStart)
            )
            TacticalKeyButton(
                label = "B",
                subLabel = "Dùng (E)",
                keySpec = PcKeyCatalog.KEY_E,
                sizeDp = (52 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_b",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            TacticalKeyButton(
                label = "A",
                subLabel = "Nhảy",
                keySpec = PcKeyCatalog.KEY_SPACE,
                sizeDp = (52 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_a",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
