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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.delay

private val GlassFillIdle = Color(0x4D0D131F)
private val GlassFillActive = Color(0x881A263D)
private val GlassRimIdle = Color(0x45E2E8F0)

/**
 * Renders a crisp PC Mouse Pointer Arrow or 3D Crosshair.
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
            val cx = size.width / 2f
            val cy = size.height / 2f
            val gap = 4.dp.toPx()
            val len = 9.dp.toPx()
            val strokeW = 1.6.dp.toPx()
            val color = if (isClicking) DangerRed else Color.White.copy(alpha = 0.85f)

            drawLine(color, Offset(cx - gap - len, cy), Offset(cx - gap, cy), strokeW, StrokeCap.Round)
            drawLine(color, Offset(cx + gap, cy), Offset(cx + gap + len, cy), strokeW, StrokeCap.Round)
            drawLine(color, Offset(cx, cy - gap - len), Offset(cx, cy - gap), strokeW, StrokeCap.Round)
            drawLine(color, Offset(cx, cy + gap), Offset(cx, cy + gap + len), strokeW, StrokeCap.Round)
            drawCircle(NeonEmerald, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
        } else {
            val s = 1.dp.toPx()
            val cursorPath = Path().apply {
                moveTo(tipX, tipY)
                lineTo(tipX, tipY + 20f * s)
                lineTo(tipX + 5.0f * s, tipY + 15.5f * s)
                lineTo(tipX + 8.5f * s, tipY + 22.5f * s)
                lineTo(tipX + 11.4f * s, tipY + 21.0f * s)
                lineTo(tipX + 7.9f * s, tipY + 14.0f * s)
                lineTo(tipX + 14.2f * s, tipY + 14.0f * s)
                close()
            }

            if (isClicking) {
                drawCircle(
                    color = NeonEmerald.copy(alpha = 0.45f),
                    radius = 8.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
            }

            drawPath(
                path = cursorPath,
                color = Color(0xEE070A12),
                style = Stroke(width = 2.4f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = cursorPath,
                color = if (isClicking) LosSantosGold else Color.White
            )
            drawPath(
                path = cursorPath,
                color = if (isClicking) DangerRed else NeonEmerald,
                style = Stroke(width = 1.0f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

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

/**
 * Console-Grade Left Analog Stick (Xbox / PS5 DualSense style smoked glass with concentric grip rings).
 */
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
        val nextShift = autoSprint && distanceRatio > 0.85f && nextW

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
                    val maxRadius = (size.width / 2f) * 0.68f

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
            val outerRadius = (size.minDimension / 2f) * 0.90f
            val knobRadius = outerRadius * 0.42f

            // Smoked-glass outer housing
            drawCircle(
                color = GlassFillIdle,
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = if (isSprinting) LosSantosGold.copy(alpha = 0.85f)
                else if (isDragging) NeonEmerald.copy(alpha = 0.75f)
                else GlassRimIdle,
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isDragging) 2.dp.toPx() else 1.2.dp.toPx())
            )

            // 4 subtle cardinal ticks on outer rim
            val tickLen = 5.dp.toPx()
            val tickColor = Color.White.copy(alpha = 0.28f)
            drawLine(tickColor, Offset(center.x, center.y - outerRadius), Offset(center.x, center.y - outerRadius + tickLen), 1.5.dp.toPx())
            drawLine(tickColor, Offset(center.x, center.y + outerRadius), Offset(center.x, center.y + outerRadius - tickLen), 1.5.dp.toPx())
            drawLine(tickColor, Offset(center.x - outerRadius, center.y), Offset(center.x - outerRadius + tickLen, center.y), 1.5.dp.toPx())
            drawLine(tickColor, Offset(center.x + outerRadius, center.y), Offset(center.x + outerRadius - tickLen, center.y), 1.5.dp.toPx())

            // Tactile concave thumbstick cap
            val knobCenter = center + knobOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xCC26334D),
                        Color(0xEE111827)
                    ),
                    center = knobCenter,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = knobCenter
            )
            // Outer cap rim
            drawCircle(
                color = if (isSprinting) LosSantosGold
                else if (isDragging) NeonEmerald
                else Color.White.copy(alpha = 0.55f),
                radius = knobRadius,
                center = knobCenter,
                style = Stroke(width = 1.8.dp.toPx())
            )
            // Inner concave grip ring
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = knobRadius * 0.62f,
                center = knobCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Text(
            text = if (isSprinting) "L3+" else "L",
            color = Color.White.copy(alpha = if (isDragging) 0.85f else 0.45f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Right Console Analog Stick for native Gamepad Right Stick (axes[2], axes[3]) + continuous 3D camera rotation.
 */
@Composable
fun RightCameraAnalogStick(
    sizeDp: Dp,
    opacity: Float,
    sensitivity: Float,
    invertY: Boolean,
    hapticsEnabled: Boolean,
    onRightStickAxesChanged: (Float, Float) -> Unit,
    onCameraDelta: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var normXState by remember { mutableFloatStateOf(0f) }
    var normYState by remember { mutableFloatStateOf(0f) }

    // Continuous 60fps camera pan while the right analog stick is deflected
    LaunchedEffect(isDragging) {
        while (isDragging) {
            if (abs(normXState) > 0.08f || abs(normYState) > 0.08f) {
                val speed = 11f * sensitivity
                val dx = normXState * speed
                val dy = normYState * speed * (if (invertY) -1f else 1f)
                onCameraDelta(dx, dy)
            }
            delay(16L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            knobOffset = Offset.Zero
            isDragging = false
            normXState = 0f
            normYState = 0f
            onRightStickAxesChanged(0f, 0f)
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .alpha(opacity)
            .testTag("right_camera_analog_stick")
            .pointerInput(hapticsEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (size.width / 2f) * 0.68f

                    fun processPos(pos: Offset) {
                        val delta = pos - center
                        val dist = hypot(delta.x, delta.y)
                        val angle = atan2(delta.y, delta.x)
                        val clamped = dist.coerceAtMost(maxRadius)
                        knobOffset = Offset(cos(angle) * clamped, sin(angle) * clamped)
                        normXState = (knobOffset.x / maxRadius).coerceIn(-1f, 1f)
                        normYState = (knobOffset.y / maxRadius).coerceIn(-1f, 1f)
                        onRightStickAxesChanged(normXState, normYState)
                    }

                    processPos(down.position)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            processPos(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    knobOffset = Offset.Zero
                    isDragging = false
                    normXState = 0f
                    normYState = 0f
                    onRightStickAxesChanged(0f, 0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f) * 0.90f
            val knobRadius = outerRadius * 0.42f

            drawCircle(color = GlassFillIdle, radius = outerRadius, center = center)
            drawCircle(
                color = if (isDragging) ElectricBlue.copy(alpha = 0.85f) else GlassRimIdle,
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isDragging) 2.dp.toPx() else 1.2.dp.toPx())
            )

            val knobCenter = center + knobOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xCC26334D), Color(0xEE111827)),
                    center = knobCenter,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = if (isDragging) ElectricBlue else Color.White.copy(alpha = 0.55f),
                radius = knobRadius,
                center = knobCenter,
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = knobRadius * 0.62f,
                center = knobCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Text(
            text = "R",
            color = Color.White.copy(alpha = if (isDragging) 0.85f else 0.45f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Authentic Console Cross-Shaped D-Pad (┼) for Gamepad D-Pad (Up/Down/Left/Right) & GTA Phone.
 */
@Composable
fun ConsoleCrossDpad(
    sizeDp: Dp,
    opacity: Float,
    hapticsEnabled: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var upPressed by remember { mutableStateOf(false) }
    var downPressed by remember { mutableStateOf(false) }
    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }

    fun updateDpadState(u: Boolean, d: Boolean, l: Boolean, r: Boolean) {
        if (u != upPressed) {
            upPressed = u
            onKeyStateChanged(PcKeyCatalog.KEY_UP, u)
        }
        if (d != downPressed) {
            downPressed = d
            onKeyStateChanged(PcKeyCatalog.KEY_DOWN, d)
        }
        if (l != leftPressed) {
            leftPressed = l
            onKeyStateChanged(PcKeyCatalog.KEY_LEFT, l)
        }
        if (r != rightPressed) {
            rightPressed = r
            onKeyStateChanged(PcKeyCatalog.KEY_RIGHT, r)
        }
    }

    DisposableEffect(Unit) {
        onDispose { updateDpadState(false, false, false, false) }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .alpha(opacity)
            .testTag("console_cross_dpad")
            .pointerInput(hapticsEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val deadzone = (size.width / 2f) * 0.20f

                    fun evaluatePos(pos: Offset) {
                        val dx = pos.x - center.x
                        val dy = pos.y - center.y
                        val dist = hypot(dx, dy)
                        if (dist < deadzone) {
                            updateDpadState(false, false, false, false)
                            return
                        }
                        val u = dy < -deadzone && abs(dy) >= abs(dx) * 0.55f
                        val d = dy > deadzone && abs(dy) >= abs(dx) * 0.55f
                        val l = dx < -deadzone && abs(dx) >= abs(dy) * 0.55f
                        val r = dx > deadzone && abs(dx) >= abs(dy) * 0.55f
                        updateDpadState(u, d, l, r)
                    }

                    evaluatePos(down.position)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            evaluatePos(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    updateDpadState(false, false, false, false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val armThickness = w * 0.34f
            val armHalf = armThickness / 2f
            val cx = w / 2f
            val cy = h / 2f
            val r = 6.dp.toPx()

            // Outer subtle circular halo
            drawCircle(
                color = Color(0x260D131F),
                radius = w * 0.48f,
                center = Offset(cx, cy)
            )

            // Vertical bar of Cross
            drawRoundRect(
                color = GlassFillIdle,
                topLeft = Offset(cx - armHalf, w * 0.06f),
                size = Size(armThickness, h * 0.88f),
                cornerRadius = CornerRadius(r, r)
            )
            // Horizontal bar of Cross
            drawRoundRect(
                color = GlassFillIdle,
                topLeft = Offset(w * 0.06f, cy - armHalf),
                size = Size(w * 0.88f, armThickness),
                cornerRadius = CornerRadius(r, r)
            )

            // Highlight active arm
            if (upPressed) {
                drawRoundRect(
                    color = NeonEmerald.copy(alpha = 0.45f),
                    topLeft = Offset(cx - armHalf, w * 0.06f),
                    size = Size(armThickness, cy - w * 0.06f),
                    cornerRadius = CornerRadius(r, r)
                )
            }
            if (downPressed) {
                drawRoundRect(
                    color = NeonEmerald.copy(alpha = 0.45f),
                    topLeft = Offset(cx - armHalf, cy),
                    size = Size(armThickness, cy - w * 0.06f),
                    cornerRadius = CornerRadius(r, r)
                )
            }
            if (leftPressed) {
                drawRoundRect(
                    color = NeonEmerald.copy(alpha = 0.45f),
                    topLeft = Offset(w * 0.06f, cy - armHalf),
                    size = Size(cx - w * 0.06f, armThickness),
                    cornerRadius = CornerRadius(r, r)
                )
            }
            if (rightPressed) {
                drawRoundRect(
                    color = NeonEmerald.copy(alpha = 0.45f),
                    topLeft = Offset(cx, cy - armHalf),
                    size = Size(cx - w * 0.06f, armThickness),
                    cornerRadius = CornerRadius(r, r)
                )
            }

            // Cross outline
            val crossPath = Path().apply {
                val m = w * 0.06f
                moveTo(cx - armHalf, m)
                lineTo(cx + armHalf, m)
                lineTo(cx + armHalf, cy - armHalf)
                lineTo(w - m, cy - armHalf)
                lineTo(w - m, cy + armHalf)
                lineTo(cx + armHalf, cy + armHalf)
                lineTo(cx + armHalf, h - m)
                lineTo(cx - armHalf, h - m)
                lineTo(cx - armHalf, cy + armHalf)
                lineTo(m, cy + armHalf)
                lineTo(m, cy - armHalf)
                lineTo(cx - armHalf, cy - armHalf)
                close()
            }
            drawPath(
                path = crossPath,
                color = GlassRimIdle,
                style = Stroke(width = 1.4.dp.toPx(), join = StrokeJoin.Round)
            )

            // Center pivot circle
            drawCircle(
                color = Color(0x44FFFFFF),
                radius = armThickness * 0.22f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Text("▲", color = if (upPressed) NeonEmerald else Color.White.copy(alpha = 0.65f), fontSize = 10.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        Text("▼", color = if (downPressed) NeonEmerald else Color.White.copy(alpha = 0.65f), fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
        Text("◀", color = if (leftPressed) NeonEmerald else Color.White.copy(alpha = 0.65f), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
        Text("▶", color = if (rightPressed) NeonEmerald else Color.White.copy(alpha = 0.65f), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
    }
}

/**
 * Curved Console Shoulder Trigger / Bumper (LT, LB, RT, RB).
 */
@Composable
fun ConsoleShoulderButton(
    label: String,
    hint: String,
    keySpec: PcKeySpec,
    widthDp: Dp = 74.dp,
    heightDp: Dp = 40.dp,
    opacity: Float = 0.8f,
    accentColor: Color = Color.White,
    hapticsEnabled: Boolean = true,
    allowDragAim: Boolean = false,
    sensitivity: Float = 1.6f,
    invertY: Boolean = false,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: ((Float, Float) -> Unit)? = null,
    testTagName: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(),
        label = "shoulder_scale"
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
            .scale(scale)
            .alpha(opacity)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
            .background(if (isPressed) accentColor.copy(alpha = 0.34f) else GlassFillIdle)
            .border(
                width = if (isPressed) 1.8.dp else 1.2.dp,
                color = if (isPressed) accentColor else GlassRimIdle,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
            )
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPressed) Color.White else accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 8.5.sp
            )
        }
    }
}

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
            subLabel = null,
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
                subLabel = null,
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
                subLabel = null,
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
                subLabel = null,
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

@Composable
fun PhoneArrowCluster(
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ConsoleCrossDpad(
        sizeDp = (104 * scale).dp,
        opacity = opacity,
        hapticsEnabled = haptics,
        onKeyStateChanged = onKeyStateChanged,
        modifier = modifier
    )
}

@Composable
fun MiniSquareKeyButton(
    label: String,
    keySpec: PcKeySpec,
    sizeDp: Dp = 36.dp,
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
            .clip(RoundedCornerShape(10.dp))
            .background(if (isPressed) accentColor.copy(alpha = 0.38f) else GlassFillIdle)
            .border(
                width = if (isPressed) 1.6.dp else 1.dp,
                color = if (isPressed) accentColor else GlassRimIdle,
                shape = RoundedCornerShape(10.dp)
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
            color = if (isPressed) Color.White else Color.White.copy(alpha = 0.88f),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

/**
 * Refined Console Face Button (Smoked-glass circular cap with subtle colored accent ring).
 */
@Composable
fun TacticalKeyButton(
    label: String,
    subLabel: String? = null,
    keySpec: PcKeySpec,
    sizeDp: Dp = 54.dp,
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
        targetValue = if (isPressed) 0.89f else 1.0f,
        animationSpec = spring(),
        label = "btn_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (active) accentColor.copy(alpha = 0.34f) else GlassFillIdle,
        label = "btn_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) accentColor else GlassRimIdle,
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
            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
            .scale(scale)
            .alpha(opacity)
            .clip(CircleShape)
            .background(bgColor)
            .border(width = if (active) 2.dp else 1.2.dp, color = borderColor, shape = CircleShape)
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
        // Subtle inner colored console ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = accentColor.copy(alpha = if (active) 0.7f else 0.32f),
                radius = (size.minDimension / 2f) * 0.80f,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (label.length == 1) 15.sp else if (sizeDp >= 64.dp) 12.sp else 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (!subLabel.isNullOrBlank()) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.88f),
                    fontSize = 7.sp,
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
            .background(if (isPressed) accentColor.copy(alpha = 0.36f) else GlassFillIdle)
            .border(
                width = if (isPressed) 2.dp else 1.2.dp,
                color = if (isPressed) accentColor else GlassRimIdle,
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
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor.copy(alpha = 0.85f),
                fontSize = 8.sp
            )
        }
    }
}
