package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MouseControlMode
import com.example.model.PcKeyCatalog
import com.example.model.PcKeySpec
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ViceMagenta
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Renders a crisp PC Mouse Pointer Arrow (Con trỏ chuột PC thực thụ) or 3D Crosshair
 * without drawing any giant circle at the user's thumb.
 */
@Composable
fun VirtualMouseCursorOverlay(
    cursorNormX: Float,
    cursorNormY: Float,
    isClicking: Boolean,
    mouseMode: MouseControlMode,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("virtual_mouse_cursor_canvas")
    ) {
        val tipX = (cursorNormX * size.width).coerceIn(2f, size.width - 2f)
        val tipY = (cursorNormY * size.height).coerceIn(2f, size.height - 2f)

        if (mouseMode == MouseControlMode.CAMERA_3D) {
            // Crisp tactical 3D center crosshair
            val cx = size.width / 2f
            val cy = size.height / 2f
            val gap = 5.dp.toPx()
            val len = 11.dp.toPx()
            val strokeW = 1.8.dp.toPx()
            val color = if (isClicking) DangerRed else NeonEmerald

            drawLine(color, Offset(cx - gap - len, cy), Offset(cx - gap, cy), strokeW, StrokeCap.Round)
            drawLine(color, Offset(cx + gap, cy), Offset(cx + gap + len, cy), strokeW, StrokeCap.Round)
            drawLine(color, Offset(cx, cy - gap - len), Offset(cx, cy - gap), strokeW, StrokeCap.Round)
            drawLine(color, Offset(cx, cy + gap), Offset(cx, cy + gap + len), strokeW, StrokeCap.Round)
            drawCircle(color, radius = 2.dp.toPx(), center = Offset(cx, cy))
        }

        // Sharp PC Mouse Cursor Arrow at (tipX, tipY)
        val s = 1.dp.toPx()
        val cursorPath = Path().apply {
            moveTo(tipX, tipY)
            lineTo(tipX, tipY + 21f * s)
            lineTo(tipX + 5.2f * s, tipY + 16.2f * s)
            lineTo(tipX + 8.8f * s, tipY + 23.5f * s)
            lineTo(tipX + 11.8f * s, tipY + 22.0f * s)
            lineTo(tipX + 8.2f * s, tipY + 14.8f * s)
            lineTo(tipX + 15.0f * s, tipY + 14.8f * s)
            close()
        }

        // Precision click indicator right at the arrow tip
        if (isClicking) {
            drawCircle(
                color = NeonEmerald.copy(alpha = 0.45f),
                radius = 9.dp.toPx(),
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = LosSantosGold,
                radius = 9.dp.toPx(),
                center = Offset(tipX, tipY),
                style = Stroke(width = 2.dp.toPx())
            )
        } else {
            // Tiny precision dot at exact click point
            drawCircle(
                color = NeonEmerald,
                radius = 2.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }

        // Dark outer shadow stroke for visibility on both bright & dark game scenes
        drawPath(
            path = cursorPath,
            color = Color(0xEE070A12),
            style = Stroke(width = 2.6f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Bright cursor body fill
        drawPath(
            path = cursorPath,
            color = if (isClicking) LosSantosGold else Color.White
        )
        // Neon cyber border
        drawPath(
            path = cursorPath,
            color = if (isClicking) DangerRed else NeonEmerald,
            style = Stroke(width = 1.1f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Full-screen/Right-half trackpad that moves the Virtual PC Mouse Cursor AND rotates the 3D camera.
 * Single-tapping anywhere on the trackpad performs a real Left Mouse Click at the cursor tip!
 */
@Composable
fun MouseLookTouchpad(
    sensitivity: Float,
    invertY: Boolean,
    hapticsEnabled: Boolean = true,
    onMouseDelta: (Float, Float, Float, Float) -> Unit,
    onTapClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var padSize by remember { mutableStateOf(IntSize(1000, 600)) }

    Box(
        modifier = modifier
            .onSizeChanged { padSize = it }
            .testTag("mouse_look_touchpad")
            .pointerInput(sensitivity, invertY, hapticsEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    var totalMoved = 0f

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val delta = change.positionChange()
                            if (delta != Offset.Zero) {
                                change.consume()
                                totalMoved += hypot(delta.x, delta.y)
                                val dx = delta.x * sensitivity
                                val dy = delta.y * sensitivity * (if (invertY) -1f else 1f)
                                onMouseDelta(
                                    dx,
                                    dy,
                                    padSize.width.toFloat().coerceAtLeast(300f),
                                    padSize.height.toFloat().coerceAtLeast(300f)
                                )
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // Quick tap on trackpad = Left Mouse Click at Virtual Cursor Tip!
                    val elapsed = System.currentTimeMillis() - downTime
                    if (totalMoved < 18.dp.toPx() && elapsed < 260L) {
                        if (hapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onTapClick()
                    }
                }
            }
    )
}

@Composable
fun VirtualAnalogStick(
    sizeDp: Dp,
    opacity: Float,
    autoSprint: Boolean,
    hapticsEnabled: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onVectorChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var isSprinting by remember { mutableStateOf(false) }

    var wPressed by remember { mutableStateOf(false) }
    var sPressed by remember { mutableStateOf(false) }
    var aPressed by remember { mutableStateOf(false) }
    var dPressed by remember { mutableStateOf(false) }
    var shiftPressed by remember { mutableStateOf(false) }

    fun updateDirectionalKeys(normX: Float, normY: Float, distanceRatio: Float) {
        val threshold = 0.28f
        val nextW = normY < -threshold
        val nextS = normY > threshold
        val nextA = normX < -threshold
        val nextD = normX > threshold
        val nextShift = autoSprint && distanceRatio > 0.84f && nextW

        if (nextW != wPressed) {
            wPressed = nextW
            onKeyStateChanged(PcKeyCatalog.KEY_W, nextW)
        }
        if (nextS != sPressed) {
            sPressed = nextS
            onKeyStateChanged(PcKeyCatalog.KEY_S, nextS)
        }
        if (nextA != aPressed) {
            aPressed = nextA
            onKeyStateChanged(PcKeyCatalog.KEY_A, nextA)
        }
        if (nextD != dPressed) {
            dPressed = nextD
            onKeyStateChanged(PcKeyCatalog.KEY_D, nextD)
        }
        if (nextShift != shiftPressed) {
            shiftPressed = nextShift
            isSprinting = nextShift
            onKeyStateChanged(PcKeyCatalog.KEY_SHIFT, nextShift)
            if (nextShift && hapticsEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
        onVectorChanged(normX, normY)
    }

    fun releaseAllStickKeys() {
        knobOffset = Offset.Zero
        isDragging = false
        isSprinting = false
        updateDirectionalKeys(0f, 0f, 0f)
    }

    DisposableEffect(Unit) {
        onDispose { releaseAllStickKeys() }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .alpha(opacity)
            .testTag("virtual_analog_stick")
            .pointerInput(autoSprint, hapticsEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (size.width / 2f) * 0.72f

                    fun processPosition(pos: Offset) {
                        val delta = pos - center
                        val dist = hypot(delta.x, delta.y)
                        val angle = atan2(delta.y, delta.x)
                        val clampedDist = dist.coerceAtMost(maxRadius)
                        knobOffset = Offset(
                            x = cos(angle) * clampedDist,
                            y = sin(angle) * clampedDist
                        )
                        val normX = (knobOffset.x / maxRadius).coerceIn(-1f, 1f)
                        val normY = (knobOffset.y / maxRadius).coerceIn(-1f, 1f)
                        val ratio = (clampedDist / maxRadius).coerceIn(0f, 1f)
                        updateDirectionalKeys(normX, normY, ratio)
                    }

                    processPosition(down.position)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            processPosition(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    releaseAllStickKeys()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f) * 0.92f
            val innerRingRadius = outerRadius * 0.65f
            val knobRadius = outerRadius * 0.34f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x880B132B), Color(0xCC070A12)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            drawCircle(
                color = if (isSprinting) LosSantosGold else if (isDragging) NeonEmerald else Color(0x6694A3B8),
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isDragging) 3.dp.toPx() else 2.dp.toPx())
            )

            drawCircle(
                color = Color(0x3300F5D4),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            if (isDragging) {
                drawLine(
                    color = if (isSprinting) LosSantosGold else NeonEmerald,
                    start = center,
                    end = center + knobOffset,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val knobCenter = center + knobOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isSprinting) {
                        listOf(LosSantosGold, Color(0xFFB45309))
                    } else if (isDragging) {
                        listOf(NeonEmerald, Color(0xFF007F6E))
                    } else {
                        listOf(Color(0xFF334155), Color(0xFF1E293B))
                    },
                    center = knobCenter,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = knobRadius,
                center = knobCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Text(
            text = if (isSprinting) "SPRINT" else "W",
            color = if (wPressed) NeonEmerald else Color.White.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp)
        )
        Text(
            text = "S",
            color = if (sPressed) NeonEmerald else Color.White.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
        )
        Text(
            text = "A",
            color = if (aPressed) NeonEmerald else Color.White.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
        )
        Text(
            text = "D",
            color = if (dPressed) NeonEmerald else Color.White.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
        )
    }
}

/**
 * Discrete WASD 4-key cluster for players who prefer individual PC directional buttons.
 */
@Composable
fun WasdKeyCluster(
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val btnSize = (50 * scale).dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TacticalKeyButton(
            label = "W",
            subLabel = "Tiến",
            keySpec = PcKeyCatalog.KEY_W,
            sizeDp = btnSize,
            opacity = opacity,
            accentColor = NeonEmerald,
            hapticsEnabled = haptics,
            onKeyStateChanged = onKeyStateChanged,
            testTagName = "btn_wasd_w"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TacticalKeyButton(
                label = "A",
                subLabel = "Trái",
                keySpec = PcKeyCatalog.KEY_A,
                sizeDp = btnSize,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_wasd_a"
            )
            TacticalKeyButton(
                label = "S",
                subLabel = "Lùi",
                keySpec = PcKeyCatalog.KEY_S,
                sizeDp = btnSize,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_wasd_s"
            )
            TacticalKeyButton(
                label = "D",
                subLabel = "Phải",
                keySpec = PcKeyCatalog.KEY_D,
                sizeDp = btnSize,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_wasd_d"
            )
        }
    }
}

/**
 * Compact GTA Phone & Menu Arrow Navigation Cluster (↑ ↓ ← → + ENTER + BACKSPACE).
 */
@Composable
fun PhoneArrowCluster(
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val keySize = (38 * scale).dp.coerceAtLeast(34.dp)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface.copy(alpha = 0.55f))
            .border(1.dp, LosSantosGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            MiniSquareKeyButton(
                label = "BACK",
                keySpec = PcKeyCatalog.KEY_BACKSPACE,
                sizeDp = keySize,
                opacity = opacity,
                accentColor = ViceMagenta,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_arrow_back"
            )
            MiniSquareKeyButton(
                label = "↑",
                keySpec = PcKeyCatalog.KEY_UP,
                sizeDp = keySize,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_arrow_up"
            )
            MiniSquareKeyButton(
                label = "OK",
                keySpec = PcKeyCatalog.KEY_ENTER,
                sizeDp = keySize,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_arrow_enter"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            MiniSquareKeyButton(
                label = "←",
                keySpec = PcKeyCatalog.KEY_LEFT,
                sizeDp = keySize,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_arrow_left"
            )
            MiniSquareKeyButton(
                label = "↓",
                keySpec = PcKeyCatalog.KEY_DOWN,
                sizeDp = keySize,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_arrow_down"
            )
            MiniSquareKeyButton(
                label = "→",
                keySpec = PcKeyCatalog.KEY_RIGHT,
                sizeDp = keySize,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_arrow_right"
            )
        }
    }
}

@Composable
fun MiniSquareKeyButton(
    label: String,
    keySpec: PcKeySpec,
    sizeDp: Dp = 40.dp,
    widthDp: Dp = sizeDp,
    opacity: Float = 0.85f,
    accentColor: Color = NeonEmerald,
    hapticsEnabled: Boolean = true,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    testTagName: String = "mini_key_${keySpec.id}",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    DisposableEffect(keySpec) {
        onDispose {
            if (isPressed) {
                isPressed = false
                onKeyStateChanged(keySpec, false)
            }
        }
    }

    Box(
        modifier = modifier
            .size(width = widthDp, height = sizeDp)
            .alpha(opacity)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) accentColor.copy(alpha = 0.45f) else CyberSurface.copy(alpha = 0.88f))
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = if (isPressed) accentColor else accentColor.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp)
            )
            .testTag(testTagName)
            .pointerInput(keySpec, hapticsEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onKeyStateChanged(keySpec, true)
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    isPressed = false
                    onKeyStateChanged(keySpec, false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPressed) Color.White else accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
fun TacticalKeyButton(
    label: String,
    subLabel: String? = null,
    keySpec: PcKeySpec,
    sizeDp: Dp = 56.dp,
    opacity: Float = 0.8f,
    accentColor: Color = NeonEmerald,
    isToggleLocked: Boolean = false,
    hapticsEnabled: Boolean = true,
    allowDragAim: Boolean = false,
    sensitivity: Float = 1.6f,
    invertY: Boolean = false,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: ((Float, Float) -> Unit)? = null,
    testTagName: String = "btn_${keySpec.id}",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val active = isPressed || isToggleLocked

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(),
        label = "btn_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (active) accentColor.copy(alpha = 0.38f) else CyberSurface.copy(alpha = 0.84f),
        label = "btn_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) accentColor else accentColor.copy(alpha = 0.55f),
        label = "btn_border"
    )

    DisposableEffect(keySpec) {
        onDispose {
            if (isPressed) {
                isPressed = false
                onKeyStateChanged(keySpec, false)
            }
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .defaultMinSize(minWidth = 42.dp, minHeight = 42.dp)
            .scale(scale)
            .alpha(opacity)
            .clip(CircleShape)
            .background(bgColor)
            .border(width = if (active) 2.5.dp else 1.5.dp, color = borderColor, shape = CircleShape)
            .testTag(testTagName)
            .pointerInput(keySpec, hapticsEnabled, allowDragAim, sensitivity, invertY) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onKeyStateChanged(keySpec, true)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val delta = change.positionChange()
                            if (allowDragAim && onMouseDelta != null && delta != Offset.Zero) {
                                change.consume()
                                val dx = delta.x * sensitivity
                                val dy = delta.y * sensitivity * (if (invertY) -1f else 1f)
                                onMouseDelta(dx, dy)
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    isPressed = false
                    onKeyStateChanged(keySpec, false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) Color.White else accentColor,
                fontSize = if (sizeDp >= 68.dp) 13.sp else if (sizeDp >= 50.dp) 11.sp else 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (!subLabel.isNullOrBlank()) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 7.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PedalButton(
    label: String,
    subLabel: String,
    keySpec: PcKeySpec,
    widthDp: Dp,
    heightDp: Dp,
    opacity: Float,
    accentColor: Color,
    hapticsEnabled: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    testTagName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(),
        label = "pedal_scale"
    )

    DisposableEffect(keySpec) {
        onDispose {
            if (isPressed) {
                isPressed = false
                onKeyStateChanged(keySpec, false)
            }
        }
    }

    Box(
        modifier = modifier
            .size(width = widthDp, height = heightDp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .scale(scale)
            .alpha(opacity)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isPressed) accentColor.copy(alpha = 0.42f) else CyberSurface.copy(alpha = 0.85f)
            )
            .border(
                width = if (isPressed) 2.5.dp else 1.5.dp,
                color = if (isPressed) accentColor else accentColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag(testTagName)
            .pointerInput(keySpec, hapticsEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onKeyStateChanged(keySpec, true)
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    isPressed = false
                    onKeyStateChanged(keySpec, false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPressed) Color.White else accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
