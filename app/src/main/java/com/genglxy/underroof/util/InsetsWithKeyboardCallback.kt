package com.genglxy.underroof.util

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

class InsetsWithKeyboardCallback(window: Window) : OnApplyWindowInsetsListener {

    private var deferredInsets = false
    private var view: View? = null
    private var lastWindowInsets: WindowInsetsCompat? = null

    init {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }
    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        view = v
        lastWindowInsets = insets

        val types = when {
            // When the deferred flag is enabled, we only use the systemBars() insets
            deferredInsets -> WindowInsetsCompat.Type.systemBars()
            // When the deferred flag is disabled, we use combination of the the systemBars() and ime() insets
            else -> WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime()
        }

        val typeInsets = insets.getInsets(types)
        v.setPadding(typeInsets.left, typeInsets.top, typeInsets.right, typeInsets.bottom)
        return WindowInsetsCompat.CONSUMED
    }
    fun onPrepare(animation: WindowInsetsAnimationCompat) {
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            // When the IME is not visible, we defer the WindowInsetsCompat.Type.ime() insets
            deferredInsets = true
        }
    }

    fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
        return insets
    }

    fun onEnd(animation: WindowInsetsAnimationCompat) {
        if (deferredInsets && (animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
            // When the IME animation has finished and the IME inset has been deferred, we reset the flag
            deferredInsets = false
            if (lastWindowInsets != null && view != null) {
                ViewCompat.dispatchApplyWindowInsets(view!!, lastWindowInsets!!)
            }
        }
    }

}