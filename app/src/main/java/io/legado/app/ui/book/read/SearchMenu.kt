package io.legado.app.ui.book.read

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.legado.app.R
import io.legado.app.databinding.ViewSearchMenuBinding
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.searchContent.SearchResult
import io.legado.app.utils.*
import splitties.views.bottomPadding
import splitties.views.leftPadding
import splitties.views.padding
import splitties.views.rightPadding

/**
 * 搜索界面菜单
 */
class SearchMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val callBack: CallBack get() = activity as CallBack
    private val binding = ViewSearchMenuBinding.inflate(LayoutInflater.from(context), this, true)

    private val bgColor: Int = context.bottomBackground
    private val textColor: Int = context.getPrimaryTextColor(ColorUtils.isColorLight(bgColor))
    private val bottomBackgroundList: ColorStateList =
        Selector.colorBuild().setDefaultColor(bgColor)
            .setPressedColor(ColorUtils.darkenColor(bgColor)).create()
    private var onMenuOutEnd: (() -> Unit)? = null

    private val searchResultList: MutableList<SearchResult> = mutableListOf()
    private var currentSearchResultIndex: Int = -1
    private var lastSearchResultIndex: Int = -1
    private val hasSearchResult: Boolean
        get() = searchResultList.isNotEmpty()
    val selectedSearchResult: SearchResult?
        get() = searchResultList.getOrNull(currentSearchResultIndex)
    val previousSearchResult: SearchResult?
        get() = searchResultList.getOrNull(lastSearchResultIndex)

    init {
        initView()
        bindEvent()
        updateSearchInfo()
    }

    fun upSearchResultList(resultList: List<SearchResult>) {
        searchResultList.clear()
        searchResultList.addAll(resultList)
        updateSearchInfo()
    }

    private fun initView() = binding.run {
        llSearchBaseInfo.setBackgroundColor(bgColor)
        tvCurrentSearchInfo.setTextColor(bottomBackgroundList)
        llBottomBg.setBackgroundColor(bgColor)
        fabLeft.backgroundTintList = bottomBackgroundList
        fabLeft.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        fabRight.backgroundTintList = bottomBackgroundList
        fabRight.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        tvMainMenu.setTextColor(textColor)
        tvSearchResults.setTextColor(textColor)
        tvSearchExit.setTextColor(textColor)
        ivMainMenu.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        ivSearchResults.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        ivSearchExit.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        ivSearchContentUp.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        ivSearchContentDown.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        tvCurrentSearchInfo.setTextColor(textColor)
    }

    private fun menuBottomInStart() {
        callBack.upSystemUiVisibility()
        binding.fabLeft.visible(hasSearchResult)
        binding.fabRight.visible(hasSearchResult)
    }

    private fun menuBottomInEnd() {
        val navigationBarHeight = if (ReadBookConfig.hideNavigationBar) {
            activity?.navigationBarHeight ?: 0
        } else {
            0
        }
        binding.run {
            vwMenuBg.setOnClickListener { runMenuOut() }
            root.padding = 0
            when (activity?.navigationBarGravity) {
                Gravity.BOTTOM -> root.bottomPadding = navigationBarHeight
                Gravity.START -> root.leftPadding = navigationBarHeight
                Gravity.END -> root.rightPadding = navigationBarHeight
            }
        }
        callBack.upSystemUiVisibility()
    }

    fun runMenuIn() {
        this.visible()
        binding.llSearchBaseInfo.visible()
        binding.llBottomBg.visible()
        binding.vwMenuBg.visible()
        menuBottomInStart()
        binding.llSearchBaseInfo.visible()
        binding.llBottomBg.visible()
        menuBottomInEnd()
    }

    private fun menuBottomOutStart() {
        binding.vwMenuBg.setOnClickListener(null)
    }

    private fun menuBottomOutEnd() {
        binding.llSearchBaseInfo.invisible()
        binding.llBottomBg.invisible()
        binding.vwMenuBg.invisible()
        binding.vwMenuBg.setOnClickListener { runMenuOut() }

        onMenuOutEnd?.invoke()
        callBack.upSystemUiVisibility()
    }

    private fun runMenuOut(onMenuOutEnd: (() -> Unit)? = null) {
        this.onMenuOutEnd = onMenuOutEnd
        if (this.isVisible) {
            menuBottomOutStart()
            binding.llSearchBaseInfo.visible(false)
            binding.llBottomBg.visible(false)
            menuBottomOutEnd()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateSearchInfo() {
        ReadBook.curTextChapter?.let {
            binding.tvCurrentSearchInfo.text =
                """${context.getString(R.string.search_content_size)}: ${searchResultList.size} / 当前章节: ${it.title}"""
        }
    }

    fun updateSearchResultIndex(updateIndex: Int) {
        lastSearchResultIndex = currentSearchResultIndex
        currentSearchResultIndex = when {
            updateIndex < 0 -> 0
            updateIndex >= searchResultList.size -> searchResultList.size - 1
            else -> updateIndex
        }
    }

    private fun bindEvent() = binding.run {
        //搜索结果
        llSearchResults.setOnClickListener {
            runMenuOut {
                callBack.openSearchActivity(selectedSearchResult?.query)
            }
        }

        //主菜单
        llMainMenu.setOnClickListener {
            runMenuOut {
                callBack.showMenuBar()
                this@SearchMenu.invisible()
            }
        }

        //退出
        llSearchExit.setOnClickListener {
            runMenuOut {
                callBack.exitSearchMenu()
            }
        }

        fabLeft.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex - 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }

        ivSearchContentUp.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex - 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }

        ivSearchContentDown.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex + 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }

        fabRight.setOnClickListener {
            updateSearchResultIndex(currentSearchResultIndex + 1)
            callBack.navigateToSearch(
                searchResultList[currentSearchResultIndex],
                currentSearchResultIndex
            )
        }
    }

    interface CallBack {
        var isShowingSearchResult: Boolean
        fun openSearchActivity(searchWord: String?)
        fun showSearchSetting()
        fun upSystemUiVisibility()
        fun exitSearchMenu()
        fun showMenuBar()
        fun navigateToSearch(searchResult: SearchResult, index: Int)
    }

}
