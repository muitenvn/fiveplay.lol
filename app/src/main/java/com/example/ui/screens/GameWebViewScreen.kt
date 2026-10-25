package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bridge.FivePlayJsBridge
import com.example.bridge.FivePlayNativeCallback
import com.example.model.MouseControlMode
import com.example.model.PcKeyCatalog
import com.example.ui.components.VirtualControlsOverlay
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.viewmodel.FivePlayUiState
import com.example.viewmodel.FivePlayViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DESKTOP_CHROME_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GameWebViewScreen(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs
    val runtime = uiState.runtime
    val coroutineScope = rememberCoroutineScope()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var chatInputText by remember { mutableStateOf("") }

    BackHandler {
        FivePlayJsBridge.releaseAllInputs(webViewRef)
        viewModel.exitToHub()
    }

    DisposableEffect(Unit) {
        onDispose {
            FivePlayJsBridge.releaseAllInputs(webViewRef)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("game_webview_screen")
    ) {
        // 1. Hardware-Accelerated Desktop-Spoofed WebView for fiveplay.lol
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("fiveplay_webview"),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(android.graphics.Color.parseColor("#070A12"))
                    isFocusable = true
                    isFocusableInTouchMode = true

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(false)
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        if (prefs.desktopUserAgent) {
                            userAgentString = DESKTOP_CHROME_UA
                        }
                    }

                    addJavascriptInterface(
                        FivePlayNativeCallback(
                            onFpsReported = { fps ->
                                viewModel.onTelemetryUpdate(fps, runtime.hasGameCanvas)
                            },
                            onCanvasDetected = { hasCanvas ->
                                viewModel.onTelemetryUpdate(runtime.currentFps, hasCanvas)
                            }
                        ),
                        "FivePlayNative"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            viewModel.onPageLoadProgress(15, true)
                            view?.evaluateJavascript(
                                FivePlayJsBridge.buildBootstrapScript(prefs.webGlFpsBoost),
                                null
                            )
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.onPageLoadProgress(100, false)
                            view?.evaluateJavascript(
                                FivePlayJsBridge.buildBootstrapScript(prefs.webGlFpsBoost),
                                null
                            )
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            viewModel.onPageLoadProgress(newProgress, newProgress < 100)
                            if (newProgress >= 45) {
                                view?.evaluateJavascript(
                                    FivePlayJsBridge.buildBootstrapScript(prefs.webGlFpsBoost),
                                    null
                                )
                            }
                        }
                    }

                    loadUrl(prefs.serverUrl)
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
                val desiredUa = if (prefs.desktopUserAgent) {
                    DESKTOP_CHROME_UA
                } else {
                    WebSettings.getDefaultUserAgent(webView.context)
                }
                if (webView.settings.userAgentString != desiredUa) {
                    webView.settings.userAgentString = desiredUa
                }
            }
        )

        // 2. Top Loading Progress Bar while connecting to fiveplay.lol
        AnimatedVisibility(
            visible = runtime.isPageLoading,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (runtime.pageLoadingProgress / 100f).coerceIn(0.05f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = NeonEmerald,
                    trackColor = CyberSurface
                )
            }
        }

        // 3. Multi-touch Virtual Gamepad, Full GTA 5 Keys & Virtual PC Mouse Cursor Overlay
        VirtualControlsOverlay(
            uiState = uiState,
            onKeyStateChanged = { spec, isDown ->
                viewModel.recordInputEvent(spec, isDown)
                FivePlayJsBridge.dispatchKeySpec(
                    webView = webViewRef,
                    spec = spec,
                    isDown = isDown,
                    cursorNormX = runtime.cursorNormX,
                    cursorNormY = runtime.cursorNormY,
                    sendNativeTapOnCursorClick = prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR
                )
            },
            onMouseDelta = { dx, dy, padW, padH ->
                if (prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR) {
                    val (nx, ny) = viewModel.moveVirtualCursorByDelta(dx, dy, padW, padH)
                    FivePlayJsBridge.dispatchMouseMoveAt(webViewRef, nx, ny, dx, dy)
                } else {
                    viewModel.updateTesterCamera(dx, dy)
                    FivePlayJsBridge.dispatchMouseMoveAt(webViewRef, 0.5f, 0.5f, dx, dy)
                }
            },
            onCursorTapClick = {
                coroutineScope.launch {
                    viewModel.setCursorClicking(true)
                    FivePlayJsBridge.dispatchKeySpec(
                        webView = webViewRef,
                        spec = PcKeyCatalog.MOUSE_LEFT,
                        isDown = true,
                        cursorNormX = runtime.cursorNormX,
                        cursorNormY = runtime.cursorNormY,
                        sendNativeTapOnCursorClick = true
                    )
                    delay(55)
                    FivePlayJsBridge.dispatchKeySpec(
                        webView = webViewRef,
                        spec = PcKeyCatalog.MOUSE_LEFT,
                        isDown = false,
                        cursorNormX = runtime.cursorNormX,
                        cursorNormY = runtime.cursorNormY,
                        sendNativeTapOnCursorClick = true
                    )
                    viewModel.setCursorClicking(false)
                }
            },
            onWheelScroll = { deltaY ->
                FivePlayJsBridge.dispatchWheel(webViewRef, deltaY)
            },
            onStickVector = { nx, ny ->
                viewModel.updateTesterStick(nx, ny)
                FivePlayJsBridge.dispatchGamepadAxes(webViewRef, nx, ny, 0f, 0f)
            },
            onRightStickVector = { rx, ry ->
                FivePlayJsBridge.dispatchRightGamepadAxes(webViewRef, rx, ry)
            },
            onCycleProfile = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                viewModel.cycleControlProfile()
            },
            onToggleMouseMode = {
                viewModel.toggleMouseControlMode()
            },
            onToggleMoveStyle = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                viewModel.toggleMovementStyle()
            },
            onToggleControls = { viewModel.toggleControlsVisibility() },
            onToggleHud = { viewModel.toggleHudExpanded() },
            onToggleMiniPcKeyboard = { viewModel.toggleMiniPcKeyboard() },
            onToggleOrientation = { viewModel.toggleOrientationLock() },
            onOpenQuickChat = {
                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_T, true)
                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_T, false)
                viewModel.setShowQuickChat(true)
            },
            onReloadPage = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                webViewRef?.reload()
            },
            onExitToHub = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                viewModel.exitToHub()
            }
        )

        // 4. Quick Chat / Keyboard Command Modal (for typing in GTA / fiveplay.lol chat or nickname)
        if (runtime.showQuickChatDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowQuickChat(false) },
                containerColor = CyberSurface,
                title = {
                    Text(
                        text = "Gõ Chat / Nhập Lệnh PC (Phím T & Enter)",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonEmerald
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Nhập nội dung chat, tên nhân vật hoặc lệnh rồi nhấn Gửi để truyền thẳng vào khung nhập của fiveplay.lol:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Ví dụ: /car zentorno hoặc Xin chào!") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_chat_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Enter", "Esc", "Backspace", "F1").forEach { quickKey ->
                                OutlinedButton(
                                    onClick = {
                                        val spec = when (quickKey) {
                                            "Enter" -> PcKeyCatalog.KEY_ENTER
                                            "Esc" -> PcKeyCatalog.KEY_ESC
                                            "Backspace" -> PcKeyCatalog.KEY_BACKSPACE
                                            else -> PcKeyCatalog.KEY_F1
                                        }
                                        FivePlayJsBridge.dispatchKeySpec(webViewRef, spec, true)
                                        FivePlayJsBridge.dispatchKeySpec(webViewRef, spec, false)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(quickKey, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                FivePlayJsBridge.dispatchChatMessage(webViewRef, chatInputText)
                                chatInputText = ""
                            } else {
                                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_ENTER, true)
                                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_ENTER, false)
                            }
                            viewModel.setShowQuickChat(false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Text("Gửi & Nhấn Enter", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowQuickChat(false) }) {
                        Text("Đóng", color = LosSantosGold)
                    }
                }
            )
        }
    }
}
