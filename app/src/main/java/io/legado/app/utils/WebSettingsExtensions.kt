package io.legado.app.utils

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebSettings
import androidx.webkit.WebSettingsCompat

/**
 * 设置是否夜间模式
 */
@SuppressLint("RequiresFeature")
fun WebSettings.denyDarkeningAllowed() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        kotlin.runCatching {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(this, false)
            return
        }.onFailure {
            it.printOnDebug()
        }
    }
}