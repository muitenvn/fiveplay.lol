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
    onRightStickVector: (Float, Float) -> Unit = { _, _ -> },
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

        // 2. Crisp Virtual PC Mouse Pointer Arrow & 3D Crosshair Layer
        VirtualMouseCursorOverlay(
            cursorNormX = runtime.cursorNormX,
            cursorNormY = runtime.cursorNormY,
            isClicking = runtime.isCursorClicking,
            mouseMode = prefs.mouseControlMode,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Profile-specific Console Controls Layer
        AnimatedVisibility(
            visible = runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            when (runtime.activeProfile) {
                ControlProfile.XBOX_GAMEPAD -> XboxGamepadLayout(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = { dx, dy -> onMouseDelta(dx, dy, 1000f, 600f) },
                    onStickVector = onStickVector,
                    onRightStickVector = onRightStickVector
                )

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

                ControlProfile.TOUCH_ONLY -> Unit
            }
        }

        // 4. Sleek Top Console Pill Bar + Optional Quick PC Key Strip
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
                    modifier = Modifier.fillMaxWidth(0.92f)
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
        color = Color(0x880B101B),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40E2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberSurface.copy(alpha = 0.75f))
                    .clickable(onClick = onToggleHud)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("hud_toggle_expand"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (runtime.currentFps >= 45) NeonEmerald else LosSantosGold)
                )
                Text(
                    text = "${runtime.currentFps} FPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Thu/Mở HUD",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }

            HudChipButton(
                icon = when (runtime.activeProfile) {
                    ControlProfile.XBOX_GAMEPAD -> Icons.Default.Gamepad
                    ControlProfile.ON_FOOT -> Icons.AutoMirrored.Filled.DirectionsRun
                    ControlProfile.DRIVING -> Icons.Default.DirectionsCar
                    ControlProfile.TOUCH_ONLY -> Icons.Default.VisibilityOff
                },
                label = when (runtime.activeProfile) {
                    ControlProfile.XBOX_GAMEPAD -> "Tay Cầm"
                    ControlProfile.ON_FOOT -> "Phím PC"
                    ControlProfile.DRIVING -> "Lái Xe"
                    ControlProfile.TOUCH_ONLY -> "Chỉ Chuột"
                },
                accent = NeonEmerald,
                onClick = onCycleProfile,
                testTag = "hud_cycle_profile_btn"
            )

            HudChipButton(
                icon = if (prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR) {
                    Icons.Default.Mouse
                } else {
                    Icons.Default.AdsClick
                },
                label = prefs.mouseControlMode.badgeVi,
                accent = LosSantosGold,
                onClick = onToggleMouseMode,
                testTag = "hud_mouse_mode_btn"
            )

            HudChipButton(
                icon = Icons.Default.Keyboard,
                label = "Bàn Phím",
                accent = if (runtime.showMiniPcKeyboard) ViceMagenta else ElectricBlue,
                onClick = onToggleMiniPcKeyboard,
                testTag = "hud_pc_keyboard_btn"
            )

            HudIconAction(
                icon = if (runtime.controlsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDesc = "Ẩn/Hiện Tay Cầm",
                tint = if (runtime.controlsVisible) NeonEmerald else Color.White,
                onClick = onToggleControls,
                testTag = "hud_visibility_btn"
            )

            if (runtime.hudExpanded) {
                if (runtime.activeProfile == ControlProfile.ON_FOOT) {
                    HudChipButton(
                        icon = Icons.Default.Gamepad,
                        label = if (prefs.movementStyle == MovementStyle.ANALOG_STICK) "Analog" else "WASD",
                        accent = ElectricBlue,
                        onClick = onToggleMoveStyle,
                        testTag = "hud_move_style_btn"
                    )
                }
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
                    tint = Color.White,
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
            .background(Color(0x55162032))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accent,
            modifier = Modifier.size(13.dp)
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
            .size(30.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Primary Console Controller Layout (Xbox Wireless / PS5 DualSense Cloud Touch Style).
 * Designed specifically for games with native HTML5 Gamepad API support + hybrid PC mouse/key fallback.
 */
@Composable
private fun XboxGamepadLayout(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    onRightStickVector: (Float, Float) -> Unit
) {
    val prefs = uiState.prefs
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. Top-Left Shoulder Bumpers & Triggers: LT (L2) + LB (L1) + View/Select (⧉)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConsoleShoulderButton(
                label = "LT",
                hint = "Ngắm/Phanh",
                keySpec = PcKeyCatalog.MOUSE_RIGHT,
                widthDp = (76 * scale).dp,
                heightDp = (40 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_lt"
            )
            ConsoleShoulderButton(
                label = "LB",
                hint = "Vũ khí",
                keySpec = PcKeyCatalog.KEY_TAB,
                widthDp = (68 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_lb"
            )
            MiniSquareKeyButton(
                label = "⧉ CAM",
                keySpec = PcKeyCatalog.KEY_V,
                sizeDp = (32 * scale).dp,
                widthDp = (48 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_select"
            )
        }

        // 2. Top-Right Shoulder Bumpers & Triggers: Start/Menu (☰) + RB (R1) + RT (R2)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniSquareKeyButton(
                label = "☰ MAP",
                keySpec = PcKeyCatalog.KEY_ESC,
                sizeDp = (32 * scale).dp,
                widthDp = (48 * scale).dp,
                opacity = opacity,
                accentColor = ViceMagenta,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_start"
            )
            ConsoleShoulderButton(
                label = "RB",
                hint = "Nấp/Drift",
                keySpec = PcKeyCatalog.KEY_Q,
                widthDp = (68 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_rb"
            )
            ConsoleShoulderButton(
                label = "RT",
                hint = "Bắn/Ga",
                keySpec = PcKeyCatalog.MOUSE_LEFT,
                widthDp = (78 * scale).dp,
                heightDp = (42 * scale).dp,
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

        // 3. Bottom-Left: Left Analog Stick (LS) + Cross-Shaped Console D-Pad (┼) + L3 Pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VirtualAnalogStick(
                sizeDp = (134 * scale).dp,
                opacity = opacity,
                autoSprint = prefs.autoSprintOnEdge,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onVectorChanged = onStickVector
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniSquareKeyButton(
                    label = "L3 (Ngồi/Còi)",
                    keySpec = PcKeyCatalog.KEY_CTRL,
                    sizeDp = (28 * scale).dp,
                    widthDp = (74 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_xbox_l3"
                )
                ConsoleCrossDpad(
                    sizeDp = (98 * scale).dp,
                    opacity = opacity,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged
                )
            }
        }

        // 4. Bottom-Right: Right Camera Analog Stick (RS) + R3 + Authentic Diamond ABXY Face Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Right Analog Stick (controls Gamepad axes[2], axes[3] + 3D Camera Look) + R3
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniSquareKeyButton(
                    label = "R3 (Nhìn sau)",
                    keySpec = PcKeyCatalog.KEY_C,
                    sizeDp = (28 * scale).dp,
                    widthDp = (74 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_xbox_r3"
                )
                RightCameraAnalogStick(
                    sizeDp = (106 * scale).dp,
                    opacity = opacity,
                    sensitivity = prefs.mouseSensitivity,
                    invertY = prefs.invertYAxis,
                    hapticsEnabled = haptics,
                    onRightStickAxesChanged = onRightStickVector,
                    onCameraDelta = onMouseDelta
                )
            }

            // Classic Console ABXY Diamond Cluster
            Box(
                modifier = Modifier.size((154 * scale).dp)
            ) {
                // Y (Top - Amber - Enter/Exit Vehicle F)
                TacticalKeyButton(
                    label = "Y",
                    subLabel = "Vào Xe",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = Color(0xFFFBBF24),
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_xbox_y",
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                // X (Left - Sky Blue - Reload / Melee R)
                TacticalKeyButton(
                    label = "X",
                    subLabel = "Nạp/Đánh",
                    keySpec = uiState.resolveKeySpec(ActionSlot.RELOAD),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = Color(0xFF60A5FA),
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_xbox_x",
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                // B (Right - Crimson - Interact / Back E)
                TacticalKeyButton(
                    label = "B",
                    subLabel = "Tương tác",
                    keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = Color(0xFFF87171),
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_xbox_b",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
                // A (Bottom - Emerald - Jump / Sprint Space)
                TacticalKeyButton(
                    label = "A",
                    subLabel = "Nhảy/Chạy",
                    keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = Color(0xFF34D399),
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_xbox_a",
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
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
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Top-Left & Top-Right Shoulder Triggers
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConsoleShoulderButton(
                label = "NGẮM",
                hint = "RMB",
                keySpec = uiState.resolveKeySpec(ActionSlot.AIM),
                widthDp = (74 * scale).dp,
                heightDp = (38 * scale).dp,
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
            ConsoleShoulderButton(
                label = "TAB",
                hint = "Vũ khí",
                keySpec = uiState.resolveKeySpec(ActionSlot.WEAPON),
                widthDp = (66 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = { spec, down ->
                    onKeyStateChanged(spec, down)
                    if (down) onWheelScroll(120)
                },
                testTagName = "btn_onfoot_weapon"
            )
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniSquareKeyButton(
                label = "ESC",
                keySpec = uiState.resolveKeySpec(ActionSlot.PAUSE),
                sizeDp = (34 * scale).dp,
                widthDp = (44 * scale).dp,
                opacity = opacity,
                accentColor = ViceMagenta,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_onfoot_esc"
            )
            MiniSquareKeyButton(
                label = "M",
                keySpec = uiState.resolveKeySpec(ActionSlot.MENU_M),
                sizeDp = (34 * scale).dp,
                widthDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_onfoot_menu"
            )
            MiniSquareKeyButton(
                label = "I",
                keySpec = uiState.resolveKeySpec(ActionSlot.INVENTORY_I),
                sizeDp = (34 * scale).dp,
                widthDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_onfoot_inv"
            )
            MiniSquareKeyButton(
                label = "V",
                keySpec = uiState.resolveKeySpec(ActionSlot.CAMERA),
                sizeDp = (34 * scale).dp,
                widthDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_onfoot_cam"
            )
        }

        // Bottom-Left Movement + Cross D-Pad + Modifiers (SHIFT, CTRL, ALT)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (prefs.movementStyle == MovementStyle.ANALOG_STICK) {
                VirtualAnalogStick(
                    sizeDp = (132 * scale).dp,
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

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniSquareKeyButton(
                        label = "SHIFT",
                        keySpec = uiState.resolveKeySpec(ActionSlot.SPRINT),
                        sizeDp = (32 * scale).dp,
                        widthDp = (48 * scale).dp,
                        opacity = opacity,
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "btn_onfoot_shift"
                    )
                    MiniSquareKeyButton(
                        label = "CTRL",
                        keySpec = uiState.resolveKeySpec(ActionSlot.CROUCH),
                        sizeDp = (32 * scale).dp,
                        widthDp = (46 * scale).dp,
                        opacity = opacity,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "btn_onfoot_ctrl"
                    )
                }
                if (prefs.showPhoneArrows) {
                    ConsoleCrossDpad(
                        sizeDp = (92 * scale).dp,
                        opacity = opacity,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged
                    )
                }
            }
        }

        // Bottom-Right Ergonomic Action Arc (F, E, R, Q, G, Space, BẮN)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                TacticalKeyButton(
                    label = "G",
                    subLabel = "Đai/Lựu",
                    keySpec = uiState.resolveKeySpec(ActionSlot.GRENADE),
                    sizeDp = (44 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_g"
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
                TacticalKeyButton(
                    label = "R",
                    subLabel = "Nạp đạn",
                    keySpec = uiState.resolveKeySpec(ActionSlot.RELOAD),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_reload"
                )
                TacticalKeyButton(
                    label = "E",
                    subLabel = "Dùng",
                    keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_interact"
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TacticalKeyButton(
                    label = "F",
                    subLabel = "Vào Xe",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                    sizeDp = (52 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_vehicle"
                )
                TacticalKeyButton(
                    label = "SPACE",
                    subLabel = "Nhảy",
                    keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                    sizeDp = (54 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_jump"
                )
                TacticalKeyButton(
                    label = "BẮN",
                    subLabel = "LMB",
                    keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                    sizeDp = (70 * scale).dp,
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
        // Top Driving Row
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConsoleShoulderButton(
                label = "F",
                hint = "Ra Xe",
                keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                widthDp = (68 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_exit"
            )
            ConsoleShoulderButton(
                label = "G",
                hint = "Dây Đai",
                keySpec = PcKeyCatalog.KEY_G,
                widthDp = (68 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_belt"
            )
            ConsoleShoulderButton(
                label = "E",
                hint = "Còi Xe",
                keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                widthDp = (66 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_horn"
            )
            ConsoleShoulderButton(
                label = "H",
                hint = "Đèn Pha",
                keySpec = PcKeyCatalog.KEY_H,
                widthDp = (66 * scale).dp,
                heightDp = (38 * scale).dp,
                opacity = opacity,
                accentColor = Color.White,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_lights"
            )
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
                label = "◄",
                subLabel = "Trái (A)",
                keySpec = PcKeyCatalog.KEY_A,
                widthDp = (78 * scale).dp,
                heightDp = (72 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_left"
            )
            PedalButton(
                label = "►",
                subLabel = "Phải (D)",
                keySpec = PcKeyCatalog.KEY_D,
                widthDp = (78 * scale).dp,
                heightDp = (72 * scale).dp,
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
                    sizeDp = (52 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_nitro"
                )
                TacticalKeyButton(
                    label = "RB",
                    subLabel = "Phanh Tay",
                    keySpec = PcKeyCatalog.KEY_SPACE,
                    sizeDp = (56 * scale).dp,
                    opacity = opacity,
                    accentColor = ViceMagenta,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_handbrake"
                )
            }
            PedalButton(
                label = "LT",
                subLabel = "Phanh/Lùi",
                keySpec = PcKeyCatalog.KEY_S,
                widthDp = (64 * scale).dp,
                heightDp = (88 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_brake"
            )
            PedalButton(
                label = "RT",
                subLabel = "Chân Ga",
                keySpec = PcKeyCatalog.KEY_W,
                widthDp = (70 * scale).dp,
                heightDp = (120 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_gas"
            )
        }
    }
}
