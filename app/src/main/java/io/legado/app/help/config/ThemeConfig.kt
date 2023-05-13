package io.legado.app.help.config

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.DisplayMetrics
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import io.legado.app.constant.EventBus
import io.legado.app.constant.PreferKey
import io.legado.app.help.DefaultData
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.model.BookCover
import io.legado.app.utils.BitmapUtils
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.FileUtils
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray
import io.legado.app.utils.fromJsonObject
import io.legado.app.utils.getPrefInt
import io.legado.app.utils.getPrefString
import io.legado.app.utils.hexString
import io.legado.app.utils.postEvent
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.putPrefInt
import io.legado.app.utils.stackBlur
import splitties.init.appCtx
import java.io.File

@Keep
object ThemeConfig {
    const val configFileName = "themeConfig.json"
    val configFilePath = FileUtils.getPath(appCtx.filesDir, configFileName)

    val configList: ArrayList<Config> by lazy {
        val cList = getConfigs() ?: DefaultData.themeConfigs
        ArrayList(cList)
    }

    fun applyDayTheme(context: Context) {
        applyTheme(context)
        disableNightMode()
        BookCover.upDefaultCover()
        postEvent(EventBus.RECREATE, "")
    }

    private fun disableNightMode() {
        val targetMode = AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(targetMode)
    }

    fun getBgImage(context: Context, metrics: DisplayMetrics): Bitmap? {
        val bgCfg = Pair(
            context.getPrefString(PreferKey.bgImage),
            context.getPrefInt(PreferKey.bgImageBlurring, 0)
        )
        if (bgCfg.first.isNullOrBlank()) return null
        val bgImage = BitmapUtils
            .decodeBitmap(bgCfg.first!!, metrics.widthPixels, metrics.heightPixels)
        if (bgCfg.second == 0) {
            return bgImage
        }
        return bgImage?.stackBlur(bgCfg.second)
    }

    fun upConfig() {
        getConfigs()?.forEach { config ->
            addConfig(config)
        }
    }

    fun save() {
        val json = GSON.toJson(configList)
        FileUtils.delete(configFilePath)
        FileUtils.createFileIfNotExist(configFilePath).writeText(json)
    }

    fun delConfig(index: Int) {
        configList.removeAt(index)
        save()
    }

    fun addConfig(json: String): Boolean {
        GSON.fromJsonObject<Config>(json.trim { it < ' ' }).getOrNull()
            ?.let {
                addConfig(it)
                return true
            }
        return false
    }

    fun addConfig(newConfig: Config) {
        configList.forEachIndexed { index, config ->
            if (newConfig.themeName == config.themeName) {
                configList[index] = newConfig
                return
            }
        }
        configList.add(newConfig)
        save()
    }

    private fun getConfigs(): List<Config>? {
        val configFile = File(configFilePath)
        if (configFile.exists()) {
            kotlin.runCatching {
                val json = configFile.readText()
                return GSON.fromJsonArray<Config>(json).getOrThrow()
            }.onFailure {
                it.printOnDebug()
            }
        }
        return null
    }

    fun applyConfig(context: Context, config: Config) {
        val primary = Color.parseColor(config.primaryColor)
        val accent = Color.parseColor(config.accentColor)
        val background = Color.parseColor(config.backgroundColor)
        val bottomBackground = Color.parseColor(config.bottomBackground)
        context.putPrefInt(PreferKey.cPrimary, primary)
        context.putPrefInt(PreferKey.cAccent, accent)
        context.putPrefInt(PreferKey.cBackground, background)
        context.putPrefInt(PreferKey.cBottomBackground, bottomBackground)
        applyDayTheme(context)
    }

    fun saveTheme(context: Context, name: String) {
        val primary = context.getPrefInt(PreferKey.cPrimary, Color.WHITE)
        val accent = context.getPrefInt(PreferKey.cAccent, Color.BLACK)
        val background = context.getPrefInt(PreferKey.cBackground, Color.WHITE)
        val bottomBackground = context.getPrefInt(PreferKey.cBottomBackground, Color.WHITE)
        val config = Config(
            themeName = name,
            primaryColor = "#${primary.hexString}",
            accentColor = "#${accent.hexString}",
            backgroundColor = "#${background.hexString}",
            bottomBackground = "#${bottomBackground.hexString}"
        )
        addConfig(config)
    }

    fun applyTheme(context: Context) = with(context) {
        val primary = getPrefInt(PreferKey.cPrimary, Color.WHITE)
        val accent = getPrefInt(PreferKey.cAccent, Color.BLACK)
        var background = getPrefInt(PreferKey.cBackground, Color.WHITE)
        if (!ColorUtils.isColorLight(background)) {
            background = Color.WHITE
            putPrefInt(PreferKey.cBackground, background)
        }
        val bottomBackground = getPrefInt(PreferKey.cBottomBackground, Color.WHITE)
        ThemeStore.editTheme(this)
            .primaryColor(ColorUtils.withAlpha(primary, 1f))
            .accentColor(ColorUtils.withAlpha(accent, 1f))
            .backgroundColor(ColorUtils.withAlpha(background, 1f))
            .bottomBackground(ColorUtils.withAlpha(bottomBackground, 1f))
            .apply()
    }

    @Keep
    data class Config(
        var themeName: String,
        var primaryColor: String,
        var accentColor: String,
        var backgroundColor: String,
        var bottomBackground: String
    ) {

        override fun hashCode(): Int {
            return GSON.toJson(this).hashCode()
        }

        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is Config) {
                return other.themeName == themeName
                        && other.primaryColor == primaryColor
                        && other.accentColor == accentColor
                        && other.backgroundColor == backgroundColor
                        && other.bottomBackground == bottomBackground
            }
            return false
        }

    }

}