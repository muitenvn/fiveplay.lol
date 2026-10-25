package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.bridge.FivePlayJsBridge
import com.example.model.ControlProfile
import com.example.viewmodel.FivePlayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FivePlay Mobile", appName)
    }

    @Test
    fun `js bootstrap script contains pointer lock and gamepad polyfills`() {
        val script = FivePlayJsBridge.buildBootstrapScript(enableWebGlBoost = true)
        assertTrue(script.contains("requestPointerLock"))
        assertTrue(script.contains("navigator.getGamepads"))
        assertTrue(script.contains("__fivePlayBridge"))
    }

    @Test
    fun `viewmodel cycles control profiles cleanly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FivePlayViewModel(app)
        assertEquals(ControlProfile.XBOX_GAMEPAD, vm.runtimeFlow.value.activeProfile)
        vm.cycleControlProfile()
        assertEquals(ControlProfile.ON_FOOT, vm.runtimeFlow.value.activeProfile)
        vm.cycleControlProfile()
        assertEquals(ControlProfile.DRIVING, vm.runtimeFlow.value.activeProfile)
    }
}
