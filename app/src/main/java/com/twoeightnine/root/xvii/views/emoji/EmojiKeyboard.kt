package com.twoeightnine.root.xvii.views.emoji

import android.content.Context
import android.graphics.Rect
import android.os.Build
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs

class EmojiKeyboard : PopupWindow {
    private var fragmentManager: androidx.fragment.app.FragmentManager? = null
    private var keyBoardHeight = 0
    private var pendingOpen: Boolean? = false
    /**

     * @return Returns true if the soft keyboard is open, false otherwise.
     */
    var isKeyBoardOpen = false
        private set

    var onEmojiClickListener: (Emoji) -> Unit

    var onEmojiconBackspaceClickedListener: ((View) -> Unit)? = null
    var onSoftKeyboardOpenCloseListener: OnSoftKeyboardOpenCloseListener? = null

    internal var rootView: View
    internal var mContext: Context
    internal var view: View? = null

    /**
     * Constructor
     * @param rootView    The top most layout in your view hierarchy. The difference of this view and the screen height will be used to calculate the keyboard height.
     */
    constructor(rootView: View, mContext: Context, emojiListener: (Emoji) -> Unit) : super(mContext) {
        this.onEmojiClickListener = emojiListener
        this.mContext = mContext
        this.rootView = rootView
        this.fragmentManager = fragmentManager
        val customView = createCustomView()
        contentView = customView
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        setSize(WindowManager.LayoutParams.MATCH_PARENT, 255)
        setBackgroundDrawable(null)

    }

    /**
     * Use this function to show the emoji popup.
     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
     * library needs you to open the soft keyboard atleast once before calling this function.
     * If that is not possible see showAtBottomPending() function.

     */
    fun showAtBottom() {
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
    }

    /**
     * Use this function when the soft keyboard has not been opened yet. This
     * will show the emoji popup after the keyboard is up next time.
     * Generally, you will be calling InputMethodManager.showSoftInput function after
     * calling this function.
     */
    fun showAtBottomPending() {
        if (isKeyBoardOpen)
            showAtBottom()
        else
            pendingOpen = true
    }

    /**
     * Call this function to resize the emoji popup according to your soft keyboard size
     */
    fun setSizeForSoftKeyboard() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)

            val screenHeight = usableScreenHeight
            var heightDifference = screenHeight - (r.bottom - r.top)
            val resourceId = mContext.resources
                    .getIdentifier("status_bar_height",
                            "dimen", "android")
            if (resourceId > 0) {
                heightDifference -= mContext.resources
                        .getDimensionPixelSize(resourceId)
            }
            if (heightDifference > 100) {
                keyBoardHeight = heightDifference
                setSize(WindowManager.LayoutParams.MATCH_PARENT, keyBoardHeight)
                if (!isKeyBoardOpen) {
                    if (onSoftKeyboardOpenCloseListener != null)
                        onSoftKeyboardOpenCloseListener!!.onKeyboardOpen(keyBoardHeight)
                }
                isKeyBoardOpen = true
                if (pendingOpen!!) {
                    showAtBottom()
                    pendingOpen = false
                }
            } else {
                isKeyBoardOpen = false
                if (onSoftKeyboardOpenCloseListener != null)
                    onSoftKeyboardOpenCloseListener!!.onKeyboardClose()
            }
        }
    }

    private val usableScreenHeight: Int
        get() = rootView.rootView.height

    /**
     * Manually set the popup window size
     * @param width Width of the popup
     * *
     * @param height Height of the popup
     */
    fun setSize(width: Int, height: Int) {
        setWidth(width)
        setHeight(height)
    }

    private fun createCustomView(): View {
        val view = View.inflate(mContext, R.layout.popup_emoji, null)
        val vpEmoji = view.findViewById<androidx.viewpager.widget.ViewPager>(R.id.viewPager)
//        val tabs = view.findViewById(R.id.tabs) as TabLayout
        val pagerAdapter = EmojiPagerAdapter {
            onEmojiClickListener.invoke(it)
            val emojis = Prefs.recentEmojis
            if (it in emojis) {
                emojis.remove(it)
            }
            emojis.add(0, it)
            if (emojis.size > 32) {
                emojis.removeAt(emojis.size - 1)
            }
            Prefs.recentEmojis = emojis
        }
        vpEmoji.adapter = pagerAdapter
        vpEmoji.currentItem = 1
        return view
    }


    interface OnSoftKeyboardOpenCloseListener {
        fun onKeyboardOpen(keyBoardHeight: Int)
        fun onKeyboardClose()
    }

}