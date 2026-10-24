package com.example.ui.screens

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenLockLandscape
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionSlot
import com.example.model.ControlProfile
import com.example.model.DEFAULT_SERVER_PRESETS
import com.example.model.MouseControlMode
import com.example.model.MovementStyle
import com.example.model.PcKeyCatalog
import com.example.ui.components.MouseLookTouchpad
import com.example.ui.components.TacticalKeyButton
import com.example.ui.components.VirtualAnalogStick
import com.example.ui.components.VirtualMouseCursorOverlay
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ViceMagenta
import com.example.viewmodel.FivePlayUiState
import com.example.viewmodel.FivePlayViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HubScreen(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val activeSlotModal = uiState.runtime.showKeybindModalForSlot

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("hub_screen"),
        containerColor = CyberBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Chơi Game") },
                    label = { Text("Chơi Ngay") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBackground,
                        selectedTextColor = NeonEmerald,
                        indicatorColor = NeonEmerald,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_play")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Tay Cầm & Phím") },
                    label = { Text("Tay Cầm & Phím") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBackground,
                        selectedTextColor = LosSantosGold,
                        indicatorColor = LosSantosGold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_controls")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Tối Ưu WebGL") },
                    label = { Text("Tối Ưu WebGL") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBackground,
                        selectedTextColor = ElectricBlue,
                        indicatorColor = ElectricBlue,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_optimize")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> PlayLauncherTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    onGoToKeymapper = { selectedTab = 1 }
                )

                1 -> ControlsKeymapperTab(
                    uiState = uiState,
                    viewModel = viewModel
                )

                2 -> WebGlOptimizerTab(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }

    if (activeSlotModal != null) {
        KeyRebindDialog(
            slot = activeSlotModal,
            currentKeyId = uiState.prefs.slotKeyBindings[activeSlotModal] ?: activeSlotModal.defaultKeyId,
            onSelectKey = { keyId -> viewModel.saveSlotBinding(activeSlotModal, keyId) },
            onDismiss = { viewModel.openKeybindModal(null) }
        )
    }
}

@Composable
private fun PlayLauncherTab(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel,
    onGoToKeymapper: () -> Unit
) {
    var customUrlInput by remember(uiState.prefs.serverUrl) {
        mutableStateOf(uiState.prefs.serverUrl)
    }
    var showCustomUrlEditor by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner Card
        item {
            LosSantosHeroCard(
                serverUrl = uiState.prefs.serverUrl,
                activeProfile = uiState.prefs.defaultProfile,
                mouseMode = uiState.prefs.mouseControlMode,
                onLaunchLandscape = { viewModel.launchGame(landscape = true) },
                onLaunchPortrait = { viewModel.launchGame(landscape = false) }
            )
        }

        // 2. Quick Control Mode & Mouse Cursor Mode Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CHẾ ĐỘ TAY CẦM & CON TRỎ CHUỘT PC",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonEmerald
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ControlProfile.entries.forEach { profile ->
                            val isSelected = uiState.prefs.defaultProfile == profile
                            val accent = when (profile) {
                                ControlProfile.ON_FOOT -> NeonEmerald
                                ControlProfile.DRIVING -> LosSantosGold
                                ControlProfile.XBOX_GAMEPAD -> ElectricBlue
                                ControlProfile.TOUCH_ONLY -> ViceMagenta
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.saveDefaultProfile(profile) }
                                    .testTag("select_profile_${profile.name.lowercase()}"),
                                color = if (isSelected) accent.copy(alpha = 0.22f) else CyberSurfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accent else Color.Transparent
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (profile) {
                                            ControlProfile.ON_FOOT -> Icons.AutoMirrored.Filled.DirectionsRun
                                            ControlProfile.DRIVING -> Icons.Default.DirectionsCar
                                            ControlProfile.XBOX_GAMEPAD -> Icons.Default.Gamepad
                                            ControlProfile.TOUCH_ONLY -> Icons.Default.TouchApp
                                        },
                                        contentDescription = profile.titleVi,
                                        tint = if (isSelected) accent else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = when (profile) {
                                            ControlProfile.ON_FOOT -> "Đi Bộ (Đủ)"
                                            ControlProfile.DRIVING -> "Lái Xe"
                                            ControlProfile.XBOX_GAMEPAD -> "Xbox Ảo"
                                            ControlProfile.TOUCH_ONLY -> "Chỉ Chuột"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Mouse Mode & Movement Style Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MouseControlMode.entries.forEach { mode ->
                            val selected = uiState.prefs.mouseControlMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.saveMouseControlMode(mode) }
                                    .testTag("select_mouse_mode_${mode.name.lowercase()}"),
                                color = if (selected) NeonEmerald.copy(alpha = 0.2f) else CyberSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (selected) NeonEmerald else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (mode == MouseControlMode.VIRTUAL_CURSOR) Icons.Default.Mouse else Icons.Default.AdsClick,
                                        contentDescription = null,
                                        tint = if (selected) NeonEmerald else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = mode.badgeVi,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        MovementStyle.entries.forEach { style ->
                            val selected = uiState.prefs.movementStyle == style
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.saveMovementStyle(style) }
                                    .testTag("select_move_style_${style.name.lowercase()}"),
                                color = if (selected) LosSantosGold.copy(alpha = 0.2f) else CyberSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (selected) LosSantosGold else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gamepad,
                                        contentDescription = null,
                                        tint = if (selected) LosSantosGold else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (style == MovementStyle.ANALOG_STICK) "Cần Analog" else "Phím WASD",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive Controller & Real Virtual PC Mouse Cursor Sandbox
        item {
            ControllerSandboxCard(
                uiState = uiState,
                viewModel = viewModel,
                onCustomizeClick = onGoToKeymapper
            )
        }

        // 4. Server Presets & Custom URL Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = LosSantosGold
                            )
                            Text(
                                text = "MÁY CHỦ WEB GAME (FIVEPLAY.LOL)",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                        TextButton(
                            onClick = { showCustomUrlEditor = !showCustomUrlEditor },
                            modifier = Modifier.testTag("toggle_custom_url_btn")
                        ) {
                            Text(
                                text = if (showCustomUrlEditor) "Ẩn sửa link" else "Đổi link/IP",
                                color = NeonEmerald
                            )
                        }
                    }

                    DEFAULT_SERVER_PRESETS.forEach { preset ->
                        val selected = uiState.prefs.serverUrl == preset.url
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    customUrlInput = preset.url
                                    viewModel.saveServerUrl(preset.url)
                                }
                                .testTag("server_preset_${preset.id}"),
                            color = if (selected) NeonEmerald.copy(alpha = 0.14f) else CyberSurfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) NeonEmerald else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Surface(
                                            color = LosSantosGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = preset.badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LosSantosGold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Đang chọn",
                                        tint = NeonEmerald
                                    )
                                }
                            }
                        }
                    }

                    if (showCustomUrlEditor) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customUrlInput,
                                onValueChange = { customUrlInput = it },
                                label = { Text("URL Máy chủ / Phòng chơi") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_server_url_input")
                            )
                            Button(
                                onClick = {
                                    viewModel.saveServerUrl(customUrlInput)
                                    showCustomUrlEditor = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Lưu Địa Chỉ Web", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LosSantosHeroCard(
    serverUrl: String,
    activeProfile: ControlProfile,
    mouseMode: MouseControlMode,
    onLaunchLandscape: () -> Unit,
    onLaunchPortrait: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.5.dp, NeonEmerald.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF170B3B),
                            Color(0xFF2E1052),
                            Color(0xFF0A1224)
                        )
                    )
                )
                val sunCenter = Offset(size.width * 0.82f, size.height * 0.35f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LosSantosGold.copy(alpha = 0.65f),
                            ViceMagenta.copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        center = sunCenter,
                        radius = size.minDimension * 0.45f
                    ),
                    radius = size.minDimension * 0.45f,
                    center = sunCenter
                )
                val horizonY = size.height * 0.68f
                drawLine(
                    color = NeonEmerald.copy(alpha = 0.35f),
                    start = Offset(0f, horizonY),
                    end = Offset(size.width, horizonY),
                    strokeWidth = 2f
                )
                for (i in 1..6) {
                    val xRatio = i / 6f
                    drawLine(
                        color = ViceMagenta.copy(alpha = 0.18f),
                        start = Offset(size.width * xRatio, horizonY),
                        end = Offset(size.width * (xRatio - 0.5f) * 2.2f + size.width * 0.5f, size.height),
                        strokeWidth = 1.5f
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = NeonEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonEmerald)
                    ) {
                        Text(
                            text = "CON TRỎ CHUỘT PC ẢO + FULL PHÍM GTA 5",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "FIVEPLAY.LOL MOBILE",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )

                Text(
                    text = "Tích hợp Con Trỏ Chuột Mũi Tên PC (di chuyển & chạm nhẹ để Click), dãy phím nóng 1-5, F1-F8, Shift, Ctrl, Alt, E, F, R, Q, G, X, C, V, cụm phím Điện Thoại GTA và Bàn Phím PC nổi đầy đủ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )

                Text(
                    text = "${activeProfile.titleVi} • ${mouseMode.badgeVi}",
                    style = MaterialTheme.typography.labelLarge,
                    color = LosSantosGold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onLaunchLandscape,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .weight(1.45f)
                            .testTag("launch_landscape_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenLockLandscape,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VÀO GAME (NGANG)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onLaunchPortrait,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, LosSantosGold),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 12.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("launch_portrait_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = LosSantosGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CHƠI DỌC",
                            style = MaterialTheme.typography.labelLarge,
                            color = LosSantosGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControllerSandboxCard(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel,
    onCustomizeClick: () -> Unit
) {
    val runtime = uiState.runtime
    val prefs = uiState.prefs
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CyberSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "THỬ CON TRỎ CHUỘT PC & TAY CẦM",
                        style = MaterialTheme.typography.titleLarge,
                        color = LosSantosGold
                    )
                    Text(
                        text = runtime.lastInputLog,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonEmerald
                    )
                }
                OutlinedButton(
                    onClick = onCustomizeClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("sandbox_customize_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gán Phím", color = NeonEmerald, style = MaterialTheme.typography.labelLarge)
                }
            }

            // Interactive Sandbox Arena with Real Virtual PC Mouse Cursor Arrow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF080D1A))
                    .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(0.42f))
                    MouseLookTouchpad(
                        sensitivity = prefs.mouseSensitivity,
                        invertY = prefs.invertYAxis,
                        hapticsEnabled = prefs.hapticFeedback,
                        onMouseDelta = { dx, dy, w, h ->
                            viewModel.moveVirtualCursorByDelta(dx, dy, w, h)
                        },
                        onTapClick = {
                            scope.launch {
                                viewModel.setCursorClicking(true)
                                viewModel.recordInputEvent(PcKeyCatalog.MOUSE_LEFT, true)
                                delay(80)
                                viewModel.recordInputEvent(PcKeyCatalog.MOUSE_LEFT, false)
                                viewModel.setCursorClicking(false)
                            }
                        },
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxSize()
                    )
                }

                // Render the exact Virtual PC Mouse Pointer Arrow inside the sandbox!
                VirtualMouseCursorOverlay(
                    cursorNormX = runtime.cursorNormX,
                    cursorNormY = runtime.cursorNormY,
                    isClicking = runtime.isCursorClicking,
                    mouseMode = prefs.mouseControlMode,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vuốt bên phải để di chuyển Con Trỏ Chuột PC • Chạm nhẹ để Click Chuột Trái",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    if (runtime.activePressedCodes.isNotEmpty()) {
                        Text(
                            text = "Đang nhấn: ${runtime.activePressedCodes.joinToString(", ")}",
                            style = MaterialTheme.typography.labelLarge,
                            color = LosSantosGold
                        )
                    }
                }

                VirtualAnalogStick(
                    sizeDp = 122.dp,
                    opacity = 0.9f,
                    autoSprint = prefs.autoSprintOnEdge,
                    hapticsEnabled = prefs.hapticFeedback,
                    onKeyStateChanged = { spec, down -> viewModel.recordInputEvent(spec, down) },
                    onVectorChanged = { nx, ny -> viewModel.updateTesterStick(nx, ny) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TacticalKeyButton(
                        label = "SHIFT",
                        subLabel = "Chạy",
                        keySpec = uiState.resolveKeySpec(ActionSlot.SPRINT),
                        sizeDp = 48.dp,
                        opacity = 0.9f,
                        accentColor = ElectricBlue,
                        hapticsEnabled = prefs.hapticFeedback,
                        onKeyStateChanged = { spec, down -> viewModel.recordInputEvent(spec, down) },
                        testTagName = "sandbox_btn_shift"
                    )
                    TacticalKeyButton(
                        label = "XE (F)",
                        subLabel = "Vào xe",
                        keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                        sizeDp = 50.dp,
                        opacity = 0.9f,
                        accentColor = LosSantosGold,
                        hapticsEnabled = prefs.hapticFeedback,
                        onKeyStateChanged = { spec, down -> viewModel.recordInputEvent(spec, down) },
                        testTagName = "sandbox_btn_vehicle"
                    )
                    TacticalKeyButton(
                        label = "NHẢY",
                        subLabel = "Space",
                        keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                        sizeDp = 52.dp,
                        opacity = 0.9f,
                        accentColor = NeonEmerald,
                        hapticsEnabled = prefs.hapticFeedback,
                        onKeyStateChanged = { spec, down -> viewModel.recordInputEvent(spec, down) },
                        testTagName = "sandbox_btn_jump"
                    )
                    TacticalKeyButton(
                        label = "BẮN",
                        subLabel = "Chuột Trái",
                        keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                        sizeDp = 64.dp,
                        opacity = 0.95f,
                        accentColor = DangerRed,
                        hapticsEnabled = prefs.hapticFeedback,
                        allowDragAim = true,
                        sensitivity = prefs.mouseSensitivity,
                        invertY = prefs.invertYAxis,
                        onKeyStateChanged = { spec, down -> viewModel.recordInputEvent(spec, down) },
                        onMouseDelta = { dx, dy -> viewModel.moveVirtualCursorByDelta(dx, dy, 800f, 400f) },
                        testTagName = "sandbox_btn_fire"
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsKeymapperTab(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel
) {
    val prefs = uiState.prefs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sliders & Cluster Visibility Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CON TRỎ CHUỘT & BỐ CỤC PHÍM",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonEmerald
                        )
                        TextButton(
                            onClick = { viewModel.resetAllBindings() },
                            modifier = Modifier.testTag("reset_bindings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = LosSantosGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mặc định", color = LosSantosGold)
                        }
                    }

                    // Mouse Look Sensitivity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tốc độ di chuyển Chuột PC / Xoay Camera 3D", color = Color.White)
                            Text(
                                "${"%.2f".format(prefs.mouseSensitivity)}x",
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = prefs.mouseSensitivity,
                            onValueChange = { viewModel.saveMouseSensitivity(it) },
                            valueRange = 0.4f..3.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonEmerald,
                                activeTrackColor = NeonEmerald
                            ),
                            modifier = Modifier.testTag("slider_mouse_sensitivity")
                        )
                    }

                    // Button Scale
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kích thước các nút bấm trên màn hình", color = Color.White)
                            Text(
                                "${(prefs.controlScale * 100).toInt()}%",
                                color = LosSantosGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = prefs.controlScale,
                            onValueChange = { viewModel.saveControlScale(it) },
                            valueRange = 0.75f..1.30f,
                            colors = SliderDefaults.colors(
                                thumbColor = LosSantosGold,
                                activeTrackColor = LosSantosGold
                            ),
                            modifier = Modifier.testTag("slider_control_scale")
                        )
                    }

                    // Button Opacity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Độ đậm/mờ phím ảo trên màn hình", color = Color.White)
                            Text(
                                "${(prefs.controlOpacity * 100).toInt()}%",
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = prefs.controlOpacity,
                            onValueChange = { viewModel.saveControlOpacity(it) },
                            valueRange = 0.30f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricBlue,
                                activeTrackColor = ElectricBlue
                            ),
                            modifier = Modifier.testTag("slider_control_opacity")
                        )
                    }

                    SettingToggleRow(
                        title = "Hiện thanh Phím Nóng phía trên (1-5, F1-F8, I, M, P, T...)",
                        subtitle = "Hiển thị dãy phím chọn súng 1-5, phím chức năng F1-F8, mở túi đồ I, bản đồ P, menu M.",
                        checked = prefs.showQuickTopBar,
                        onCheckedChange = { viewModel.saveShowQuickTopBar(it) },
                        testTag = "switch_quick_top_bar"
                    )

                    SettingToggleRow(
                        title = "Hiện cụm phím Mũi Tên điều khiển Điện Thoại GTA (↑ ↓ ← →)",
                        subtitle = "Hiển thị cụm phím mũi tên + Enter + Backspace ở cạnh trái để dùng điện thoại trong GTA 5.",
                        checked = prefs.showPhoneArrows,
                        onCheckedChange = { viewModel.saveShowPhoneArrows(it) },
                        testTag = "switch_phone_arrows"
                    )
                }
            }
        }

        // 2. Interactive Key Rebinding List (20 Action Slots)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BẢNG GÁN PHÍM BÀN PHÍM & CHUỘT PC (20 NÚT)",
                        style = MaterialTheme.typography.titleLarge,
                        color = LosSantosGold
                    )
                    Text(
                        text = "Chạm vào bất kỳ nút nào bên dưới để đổi sang phím PC/Chuột bất kỳ (WASD, F1-F8, 1-6, Alt, Ctrl, Shift, X, Z, B, N, Y, U, I, P, K, L...):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    ActionSlot.entries.forEach { slot ->
                        val boundSpec = uiState.resolveKeySpec(slot)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.openKeybindModal(slot) }
                                .testTag("rebind_${slot.slotId}"),
                            color = CyberSurfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = slot.titleVi,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Ký hiệu nút: ${slot.shortBadge}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    color = NeonEmerald.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = boundSpec.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = NeonEmerald
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Đổi phím",
                                            tint = NeonEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebGlOptimizerTab(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel
) {
    val prefs = uiState.prefs
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CyberSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "TỐI ƯU HÓA HIỆU NĂNG WEBGL & TRÌNH DUYỆT",
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricBlue
                    )

                    SettingToggleRow(
                        title = "Giả lập Trình duyệt PC (Desktop User-Agent)",
                        subtitle = "Bắt buộc bật để fiveplay.lol nhận diện thiết bị như máy tính Windows Chrome 131 và mở khóa toàn bộ tính năng.",
                        checked = prefs.desktopUserAgent,
                        onCheckedChange = { viewModel.saveDesktopUserAgent(it) },
                        testTag = "switch_desktop_ua"
                    )

                    SettingToggleRow(
                        title = "Tối ưu độ phân giải WebGL (Tăng FPS máy yếu)",
                        subtitle = "Giới hạn tỷ lệ điểm ảnh (devicePixelRatio 1.25x) giúp GPU điện thoại chạy mượt 60 FPS và giảm nóng máy.",
                        checked = prefs.webGlFpsBoost,
                        onCheckedChange = { viewModel.saveWebGlFpsBoost(it) },
                        testTag = "switch_webgl_boost"
                    )

                    SettingToggleRow(
                        title = "Tự động Chạy nhanh (Shift) khi đẩy kịch cần Analog",
                        subtitle = "Khi đẩy cần Analog trái lên sát viền trên, nhân vật tự động giữ phím Shift để chạy nước rút.",
                        checked = prefs.autoSprintOnEdge,
                        onCheckedChange = { viewModel.saveAutoSprintOnEdge(it) },
                        testTag = "switch_auto_sprint"
                    )

                    SettingToggleRow(
                        title = "Rung phản hồi xúc giác (Haptic Feedback)",
                        subtitle = "Rung nhẹ khi nhấn các phím Bắn, Chân Ga, Phanh và Click chuột.",
                        checked = prefs.hapticFeedback,
                        onCheckedChange = { viewModel.saveHapticFeedback(it) },
                        testTag = "switch_haptics"
                    )

                    SettingToggleRow(
                        title = "Đảo ngược trục dọc Camera (Invert Y-Axis)",
                        subtitle = "Đảo chiều vuốt lên/xuống khi xoay góc nhìn chuột 3D.",
                        checked = prefs.invertYAxis,
                        onCheckedChange = { viewModel.saveInvertYAxis(it) },
                        testTag = "switch_invert_y"
                    )

                    OutlinedButton(
                        onClick = {
                            CookieManager.getInstance().removeAllCookies(null)
                            WebStorage.getInstance().deleteAllData()
                            Toast.makeText(
                                context,
                                "Đã dọn sạch bộ nhớ đệm WebGL & Cookie!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ViceMagenta),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_webview_cache_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = ViceMagenta
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dọn Bộ Nhớ Đệm WebView (Làm mới tài nguyên Game)", color = ViceMagenta)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "XUẤT FILE APK QUA GITHUB ACTIONS & RELEASES",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonEmerald
                    )
                    Text(
                        text = "Dự án đã được cấu hình sẵn file workflow '.github/workflows/build-apk-release.yml':\n" +
                            "• Cách 1 (Qua GitHub Releases): Nhấn nút Push to GitHub trên thanh công cụ -> Mở kho lưu trữ GitHub của bạn, GitHub Actions sẽ tự động biên dịch và đăng file 'FivePlay-Mobile-Release-Signed.apk' vào mục Releases (hoặc bạn vào tab Actions -> chọn 'Build & Release Android APK' -> nhấn 'Run workflow').\n" +
                            "• Cách 2 (Trực tiếp): Bạn cũng có thể tải trực tiếp file APK ngay trong menu Cài đặt (Settings -> Generate APK) của AI Studio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberBackground,
                checkedTrackColor = NeonEmerald
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeyRebindDialog(
    slot: ActionSlot,
    currentKeyId: String,
    onSelectKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = {
            Text(
                text = "Gán Phím Cho: ${slot.titleVi}",
                style = MaterialTheme.typography.titleLarge,
                color = NeonEmerald
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chọn phím Bàn phím hoặc Chuột PC bạn muốn kích hoạt khi nhấn nút này:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PcKeyCatalog.ALL_KEYS.forEach { keySpec ->
                        val isSelected = keySpec.id == currentKeyId
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectKey(keySpec.id) }
                                .testTag("pick_key_${keySpec.id}"),
                            color = if (isSelected) NeonEmerald else CyberSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = keySpec.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = LosSantosGold)
            }
        }
    )
}
