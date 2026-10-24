package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PcKeyCatalog
import com.example.model.PcKeySpec
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HudGlass
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ViceMagenta

/**
 * Horizontal Quick-Key Strip for GTA 5 Weapons (1-5), Function Keys (F1-F8), Inventory (I), Map (P/ESC), Menu (M), etc.
 */
@Composable
fun QuickTopKeyStrip(
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickKeys = listOf(
        PcKeyCatalog.KEY_ESC to ViceMagenta,
        PcKeyCatalog.KEY_TAB to LosSantosGold,
        PcKeyCatalog.KEY_1 to NeonEmerald,
        PcKeyCatalog.KEY_2 to NeonEmerald,
        PcKeyCatalog.KEY_3 to NeonEmerald,
        PcKeyCatalog.KEY_4 to NeonEmerald,
        PcKeyCatalog.KEY_5 to NeonEmerald,
        PcKeyCatalog.KEY_I to ElectricBlue,
        PcKeyCatalog.KEY_M to ElectricBlue,
        PcKeyCatalog.KEY_P to ElectricBlue,
        PcKeyCatalog.KEY_T to LosSantosGold,
        PcKeyCatalog.KEY_Y to ElectricBlue,
        PcKeyCatalog.KEY_U to ElectricBlue,
        PcKeyCatalog.KEY_K to ElectricBlue,
        PcKeyCatalog.KEY_L to LosSantosGold,
        PcKeyCatalog.KEY_B to NeonEmerald,
        PcKeyCatalog.KEY_Z to NeonEmerald,
        PcKeyCatalog.KEY_F1 to ViceMagenta,
        PcKeyCatalog.KEY_F2 to ViceMagenta,
        PcKeyCatalog.KEY_F3 to ViceMagenta,
        PcKeyCatalog.KEY_F5 to ViceMagenta,
        PcKeyCatalog.KEY_F6 to ViceMagenta,
        PcKeyCatalog.KEY_F8 to DangerRed
    )

    Row(
        modifier = modifier
            .alpha(opacity)
            .clip(RoundedCornerShape(12.dp))
            .background(HudGlass)
            .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("quick_top_key_strip"),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        quickKeys.forEach { (spec, color) ->
            MiniSquareKeyButton(
                label = spec.shortText,
                keySpec = spec,
                sizeDp = 32.dp,
                widthDp = if (spec.shortText.length >= 3) 42.dp else 34.dp,
                opacity = 0.95f,
                accentColor = color,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "strip_key_${spec.id}"
            )
        }
    }
}

/**
 * Full On-Screen PC Gaming Keyboard Overlay so NO PC key is ever missing.
 */
@Composable
fun MiniPcKeyboardOverlay(
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rowFn = listOf(
        PcKeyCatalog.KEY_ESC, PcKeyCatalog.KEY_F1, PcKeyCatalog.KEY_F2, PcKeyCatalog.KEY_F3,
        PcKeyCatalog.KEY_F5, PcKeyCatalog.KEY_F6, PcKeyCatalog.KEY_F8,
        PcKeyCatalog.KEY_1, PcKeyCatalog.KEY_2, PcKeyCatalog.KEY_3, PcKeyCatalog.KEY_4, PcKeyCatalog.KEY_5, PcKeyCatalog.KEY_6
    )
    val rowQwerty = listOf(
        PcKeyCatalog.KEY_TAB,
        PcKeySpec("K_Q", "Q", "Q", "q", "KeyQ", 81),
        PcKeySpec("K_W", "W", "W", "w", "KeyW", 87),
        PcKeySpec("K_E", "E", "E", "e", "KeyE", 69),
        PcKeySpec("K_R", "R", "R", "r", "KeyR", 82),
        PcKeySpec("K_T", "T", "T", "t", "KeyT", 84),
        PcKeySpec("K_Y", "Y", "Y", "y", "KeyY", 89),
        PcKeySpec("K_U", "U", "U", "u", "KeyU", 85),
        PcKeySpec("K_I", "I", "I", "i", "KeyI", 73),
        PcKeySpec("K_O", "O", "O", "o", "KeyO", 79),
        PcKeySpec("K_P", "P", "P", "p", "KeyP", 80),
        PcKeyCatalog.KEY_BACKSPACE
    )
    val rowAsdf = listOf(
        PcKeyCatalog.KEY_SHIFT,
        PcKeySpec("K_A", "A", "A", "a", "KeyA", 65),
        PcKeySpec("K_S", "S", "S", "s", "KeyS", 83),
        PcKeySpec("K_D", "D", "D", "d", "KeyD", 68),
        PcKeySpec("K_F", "F", "F", "f", "KeyF", 70),
        PcKeySpec("K_G", "G", "G", "g", "KeyG", 71),
        PcKeySpec("K_H", "H", "H", "h", "KeyH", 72),
        PcKeySpec("K_J", "J", "J", "j", "KeyJ", 74),
        PcKeySpec("K_K", "K", "K", "k", "KeyK", 75),
        PcKeySpec("K_L", "L", "L", "l", "KeyL", 76),
        PcKeyCatalog.KEY_ENTER
    )
    val rowZxcv = listOf(
        PcKeyCatalog.KEY_CTRL,
        PcKeyCatalog.KEY_ALT,
        PcKeySpec("K_Z", "Z", "Z", "z", "KeyZ", 90),
        PcKeySpec("K_X", "X", "X", "x", "KeyX", 88),
        PcKeySpec("K_C", "C", "C", "c", "KeyC", 67),
        PcKeySpec("K_V", "V", "V", "v", "KeyV", 86),
        PcKeySpec("K_B", "B", "B", "b", "KeyB", 66),
        PcKeySpec("K_N", "N", "N", "n", "KeyN", 78),
        PcKeySpec("K_M", "M", "M", "m", "KeyM", 77),
        PcKeyCatalog.KEY_SPACE,
        PcKeyCatalog.KEY_LEFT,
        PcKeyCatalog.KEY_UP,
        PcKeyCatalog.KEY_DOWN,
        PcKeyCatalog.KEY_RIGHT
    )

    Surface(
        modifier = modifier
            .fillMaxWidth(0.94f)
            .testTag("mini_pc_keyboard_overlay"),
        color = Color(0xEB080D1A),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, NeonEmerald.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BÀN PHÍM PC ĐẦY ĐỦ (NHẤN PHÍM BẤT KỲ TRONG GAME)",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonEmerald,
                    fontSize = 11.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DangerRed.copy(alpha = 0.25f))
                        .border(1.dp, DangerRed, RoundedCornerShape(8.dp))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("close_mini_pc_keyboard_btn")
                ) {
                    Text(
                        text = "ĐÓNG BÀN PHÍM ✕",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            listOf(rowFn, rowQwerty, rowAsdf, rowZxcv).forEach { row ->
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { spec ->
                        val isWide = spec.shortText.length >= 4
                        val accent = when {
                            spec.id.startsWith("KEY_F") || spec.id == "KEY_ESC" -> ViceMagenta
                            spec.id in listOf("KEY_SHIFT", "KEY_CTRL", "KEY_ALT", "KEY_SPACE", "KEY_ENTER", "KEY_TAB") -> LosSantosGold
                            else -> NeonEmerald
                        }
                        MiniSquareKeyButton(
                            label = spec.shortText,
                            keySpec = spec,
                            sizeDp = 34.dp,
                            widthDp = if (spec.id == "KEY_SPACE") 74.dp else if (isWide) 50.dp else 36.dp,
                            opacity = 0.95f,
                            accentColor = accent,
                            hapticsEnabled = haptics,
                            onKeyStateChanged = onKeyStateChanged,
                            testTagName = "pckb_${spec.id}"
                        )
                    }
                }
            }
        }
    }
}
