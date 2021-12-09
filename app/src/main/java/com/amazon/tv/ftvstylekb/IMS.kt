package com.amazon.tv.ftvstylekb

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.inputmethod.*
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.amazon.tv.ftvstylekb.SettingsActivity.Companion.setLauncherIconShown
import java.util.*


class IMS : InputMethodService() {
    private val firstFocus = R.id.cr00
    private var ic: InputConnection? = null
    private var kv: View? = null
    private var keyboardView: View? = null
    private var preview: TextView? = null
    private var keyRu: Array<String> =
        LangSymbols.KEY_RU_YCU // LangSymbols.KEY_RU_ABV | LangSymbols.KEY_RU_YCU
    private var keyEn: Array<String> =
        LangSymbols.KEY_EN_QWE // LangSymbols.KEY_EN_ABC | LangSymbols.KEY_EN_QWE
    private val allSymbols: Array<String> = LangSymbols.SYMBOLS
    private var cursorEnd = 0
    private var extractedText: ExtractedText? = null
    private var close = false
    private val style = false
    private var btnClear: IconButton? = null
    private var btnLang: IconButton? = null
    private var btnSpace: IconButton? = null
    private var btnBack: IconButton? = null
    private var btnDelete: IconButton? = null
    private var btnNext: IconButton? = null
    private var btnSymbols: IconButton? = null
    private var btnShift: IconButton? = null
    private var btnCursorLeft: IconButton? = null
    private var btnCursorRight: IconButton? = null
    private var shiftKey = false
    private var capsKey = false
    private var keyboard = true
    private var symbol = false
    private var m = 0
    private var s = 0
    private var handlerStart: Handler? = null
    private var runnableStart: Runnable? = null
    private var handlerCaps: Handler? = null
    private var runnableCaps: Runnable? = null
    private var parentView: ViewGroup? = null
    private var colors: ColorStateList? = null
    private var colorsSup: ColorStateList? = null
    private var textDpSize = 0
    private var textDpSizeFull = 0
    private var cursorStart = 0
    private var textLength = 0
    private var firstButtonText: String? = null
    private var secondPress = false
    private var trueKeyboard = false
    private var trueSymbol = false
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var doubleDouble = false
    private var focusButton: IconButton? = null
    private var blockKey = false
    private var deleteSpeed = 1
    private var actionEnter = 0
    private var inputType = 0
    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            enableHardwareAcceleration()
        }
        super.onCreate()
        val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateInputView(): View? {
        keyboardView = layoutInflater.inflate(R.layout.keyboard, null)
        return null
    }

    override fun onCreateCandidatesView(): View? {
        val context = applicationContext
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getString(context.getString(R.string.app_layout_pref_key), "qwerty") == "abc") {
            keyRu = LangSymbols.KEY_RU_ABV
            keyEn = LangSymbols.KEY_EN_ABC
        }
        kv = if (keyboard) setLang(keyRu) else setLang(keyEn)
        val frameLayout = keyboardView?.findViewById<FrameLayout>(R.id.keyboard1)
        frameLayout?.addView(kv)
        keyboardView?.invalidate()
        preview = keyboardView?.findViewById(R.id.preview)
        if (preview?.text.isNullOrEmpty())
            preview?.visibility = View.INVISIBLE
        return keyboardView!!
    }

    override fun onStartInput(editorinfo: EditorInfo, flag: Boolean) {
        super.onStartInput(editorinfo, flag)
        setImeOptions(resources, editorinfo)
        //if(kv!=null) {
        //btnBack.setPressed(false);
        //kv.findViewById(firstFocus).requestFocusFromTouch();
        //if(kv.isInTouchMode()) {
        //kv.findViewById(firstFocus).requestFocusFromTouch();
        //} else kv.findViewById(firstFocus).requestFocus();
        //}
        //setBackDisposition(BACK_DISPOSITION_WILL_DISMISS);
    }

    override fun onFinishInputView(flag: Boolean) {
        m = 0
        s = 0
        handlerStart?.removeCallbacks(runnableStart!!)
        super.onFinishInputView(flag)
        if (kv != null) {
            focusButton?.isPressed = false
            btnBack?.isPressed = false
            btnSpace?.isPressed = false
            btnDelete?.isPressed = false
            btnNext?.isPressed = false
            btnLang?.isPressed = false
        }
        //ic=null;
    }

    override fun onStartInputView(editorinfo: EditorInfo, flag: Boolean) {
        super.onStartInputView(editorinfo, flag)
        setImeOptions(resources, editorinfo)
        updateInputViewShown()
        if (close)
            handleClose()
        else {
            ic = IcWrapper(currentInputConnection, false)
            cursorEnd = 0
            cursorStart = 0
            textLength = 0
            ic?.let { lc ->
                //btnNext.setText(String.valueOf(ic));
                extractedText = lc.getExtractedText(ExtractedTextRequest(), 0)
                extractedText?.let { et ->
                    if (et.selectionEnd == 0 && et.text.length < 101) {
                        cursorEnd = et.text.length
                        lc.setSelection(cursorEnd, cursorEnd)
                    }
                }
            }
            if (symbol)
                btnLang?.performClick()
            try {
                handlerStart = Handler()
                runnableStart = Runnable {
                    kv?.let {
                        if (isInputViewShown && !it.hasFocus() && !close) {
                            it.findViewById<View>(firstFocus).requestFocusFromTouch()
                            handlerStart?.postDelayed(runnableStart!!, 300)
                        }
                    }
                }
                handlerStart?.postDelayed(runnableStart!!, 200)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //btnNext.setText(String.valueOf(actionEnter));
    }

    private fun mySetAllCaps(textDpSize: Int) {
        s++
        if (s == 1) {
            handlerCaps = Handler()
            runnableCaps = Runnable { s = 0 }
            shiftKey = !shiftKey
            if (capsKey) {
                capsKey = false
            }
            spannableTextAndColorShift(btnShift, LangSymbols.А_А, textDpSizeFull)
            for (i in 0..39) {
                val childButton = parentView?.getChildAt(i) as IconButton
                spannableTextAndColor(
                    childButton,
                    childButton.text.toString(),
                    textDpSize,
                    colorsSup
                )
            }
            btnLang?.isAllCaps = shiftKey
            handlerCaps?.postDelayed(runnableCaps!!, 300)
        }
        if (s == 2) {
            s = 0
            handlerCaps?.removeCallbacks(runnableCaps!!)
            if (shiftKey) {
                capsKey = true
                spannableTextAndColorShift(btnShift, LangSymbols.А_А, textDpSizeFull)
            }
        } else
            spannableTextAndColorShift(btnShift, LangSymbols.А_А, textDpSizeFull)
    }

    private fun changeLang(lang: Array<String>, textDpSize: Int) {
        if (symbol) {
            btnBack?.nextFocusUpId = R.id.cr32
            btnLang?.nextFocusRightId = R.id.space
            btnSpace?.nextFocusLeftId = R.id.lang
            kv?.findViewById<View>(R.id.cr32)?.nextFocusDownId = R.id.back
            kv?.findViewById<View>(R.id.cr33)?.nextFocusDownId = R.id.back
        } else {
            btnBack?.nextFocusUpId = R.id.shift
            btnLang?.nextFocusRightId = R.id.shift
            btnSpace?.nextFocusLeftId = R.id.symbols
            kv?.findViewById<View>(R.id.cr32)?.nextFocusDownId = R.id.shift
            kv?.findViewById<View>(R.id.cr33)?.nextFocusDownId = R.id.symbols
        }
        for (i in 0..39) {
            val childButton = parentView?.getChildAt(i) as IconButton
            val childButtonText = lang[i]
            spannableTextAndColor(childButton, childButtonText, textDpSize, colorsSup)
            //childButton.setText(lang[i]);
        }
    }

    private fun setLang(symbols: Array<String>): View? {
        val keySize = (resources.getDimension(R.dimen.key_size) + 0.5f).toInt()
        val marginSize = (resources.getDimension(R.dimen.keyboard_margin) + 0.5f).toInt()
        textDpSize = (resources.getDimension(R.dimen.input_text_size) + 0.5f).toInt()
        textDpSizeFull = (resources.getDimension(R.dimen.input_text_size_full) + 0.5f).toInt()
        kv = layoutInflater.inflate(R.layout.grid_layout, null, false)

        kv?.let { kv ->
            //style = true
            if (style) {
                kv.setBackgroundResource(R.color.color_btn_ftv)
            }
            btnClear = kv.findViewById(R.id.clear)
            btnLang = kv.findViewById(R.id.lang)
            btnSpace = kv.findViewById(R.id.space)
            btnBack = kv.findViewById(R.id.back)
            btnDelete = kv.findViewById(R.id.delete)
            btnNext = kv.findViewById(R.id.next)
            btnSymbols = kv.findViewById(R.id.symbols)
            btnShift = kv.findViewById(R.id.shift)
            btnCursorLeft = kv.findViewById(R.id.cursorLeft)
            btnCursorRight = kv.findViewById(R.id.cursorRight)
            parentView = kv as ViewGroup?
            for (i in 0 until parentView!!.childCount) {
                val childButton = parentView?.getChildAt(i) as IconButton
                val id = childButton.id
                if (style) {
                    childButton.setBackgroundResource(R.drawable.btn_shape)
                }
                childButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textDpSize.toFloat())
                try {
                    val parser = resources.getXml(R.xml.btn_text_color)
                    val parserSup = resources.getXml(R.xml.btn_sup_text_color)
                    colors = ColorStateList.createFromXml(resources, parser)
                    colorsSup = ColorStateList.createFromXml(resources, parserSup)
                    childButton.setTextColor(colors)
                    spannableTextAndColor(childButton, symbols[i], textDpSize, colorsSup)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                //childButton.setTextColor(getResources().getColor(R.button_text_color));
                childButton.isAllCaps = false
                val params = childButton.layoutParams as GridLayout.LayoutParams
                params.setMargins(marginSize, marginSize, marginSize, marginSize)
                params.width = keySize
                params.height = keySize
                childButton.layoutParams = params
                childButton.setOnLongClickListener(OnLongClickListener {
                    if (capsKey) {
                        btnShift?.performClick()
                        return@OnLongClickListener true
                    }
                    false
                })
                childButton.setOnClickListener {
                    if (firstButtonText != null && m == 1 && secondPress && !firstButtonText.equals(
                            childButton.text.toString(),
                            ignoreCase = true
                        )
                    ) {
                        m = 0
                        if (shiftKey || capsKey) {
                            ic?.commitText(
                                firstButtonText!!.substring(0, 1).uppercase(Locale.getDefault()), 1
                            )
                            if (!capsKey)
                                mySetAllCaps(textDpSize)
                        } else {
                            ic?.commitText(
                                firstButtonText!!.substring(0, 1).lowercase(Locale.getDefault()), 1
                            )
                        }
                    }
                    extractedText = ic?.getExtractedText(ExtractedTextRequest(), 0)
                    extractedText?.let { et ->
                        cursorStart = et.selectionEnd
                        textLength = et.text.length
                    }
                    when (id) {
                        R.id.cursorLeft -> if (cursorStart > 0) ic?.setSelection(
                            cursorStart - 1,
                            cursorStart - 1
                        )
                        R.id.cursorRight -> if (cursorStart < textLength) ic?.setSelection(
                            cursorStart + 1,
                            cursorStart + 1
                        )
                        R.id.lang -> {
                            if (symbol) {
                                symbol = false
                                keyboard = !keyboard
                                disableButton(btnSymbols, btnShift)
                            }
                            if (keyboard) {
                                keyboard = false
                                changeLang(keyEn, textDpSize)
                                btnLang?.text = LangSymbols.ABV
                            } else {
                                keyboard = true
                                changeLang(keyRu, textDpSize)
                                btnLang?.text = LangSymbols.ABC
                            }
                        }
                        R.id.space -> ic?.commitText(" ", 1)
                        R.id.delete -> if (ic?.getSelectedText(0).isNullOrEmpty())
                            ic?.deleteSurroundingText(deleteSpeed, 0)
                        else
                            ic?.commitText("", 1)
                        R.id.clear -> try {
                            extractedText = ic?.getExtractedText(ExtractedTextRequest(), 0)
                            extractedText?.let { et ->
                                ic?.deleteSurroundingText(
                                    et.selectionStart - 0,
                                    et.text.length - et.selectionEnd
                                )
                            }
                            //ic.performContextMenuAction(android.R.id.selectAll);
                            //ic.commitText("", 1);
                        } catch (e: Exception) {
                        }
                        R.id.back -> handleClose()
                        R.id.next -> if (inputType == InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE ||
                            inputType == InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        )
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                        else
                            ic?.performEditorAction(actionEnter)
                        R.id.symbols -> {
                            symbol = true
                            changeLang(allSymbols, textDpSize)
                            if (keyboard)
                                btnLang?.text = LangSymbols.ABV
                            else
                                btnLang?.text = LangSymbols.ABC
                            disableButton(btnSymbols, btnShift)
                            btnLang?.requestFocusFromTouch()
                        }
                        R.id.shift -> mySetAllCaps(textDpSize)
                        else -> {
                            val childBtnText = childButton.text.toString()
                            ic?.let {
                                if (doubleClickButton(childButton, childBtnText, textDpSize)) {
                                    // No action with doubleClickButton
                                } else if (shiftKey || capsKey) {
                                    it.commitText(
                                        childBtnText.substring(0, 1).uppercase(Locale.getDefault()),
                                        1
                                    )
                                    //shiftKey=!shiftKey;
                                    if (!capsKey)
                                        mySetAllCaps(textDpSize)
                                    else {
                                    }
                                    //spannableTextAndColorShift(btnShift, "аА", textDpSizeFull);
                                } else {
                                    //ic.commitText(childBtnText.toLowerCase(), 1);
                                    it.commitText(
                                        childBtnText.substring(0, 1).lowercase(Locale.getDefault()),
                                        1
                                    )
                                }
                            }
                        }
                    }
                }
            }
            largeButtonTextSize(
                textDpSizeFull,
                btnNext,
                btnBack,
                btnSpace,
                btnLang,
                btnDelete,
                btnClear,
                btnSymbols
            )
            largeButtonTextBackground(btnNext, btnBack)
            spannableTextAndColorShift(btnShift, LangSymbols.А_А, textDpSizeFull)
        }
        return kv
    }

    private fun startSettings(): Boolean {
        try {
            setLauncherIconShown(applicationContext, SettingsActivity::class.java, true)
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun disableButton(vararg btn: IconButton?) {
        val alpha: Float = if (symbol) 0.25f else 1f
        for (b in btn) {
            b?.let {
                it.isFocusable = !symbol
                it.isEnabled = !symbol
                it.alpha = alpha
                it.invalidate()
            }
        }
    }

    private fun spannableTextAndColorShift(
        childButton: IconButton?,
        btnText: String,
        textDpSizeFull: Int
    ) {
        val shiftText: SpannableString =
            if (capsKey) SpannableString(btnText.uppercase(Locale.getDefault())) else SpannableString(
                btnText
            )
        var focusedCapsOn = resources.getColor(R.color.color_background_ftv)
        var colorCapsOn = resources.getColor(R.color.button_text_color)
        var colorCapsOff = resources.getColor(R.color.color_transparent_shift)
        var focusedCapsOff = resources.getColor(R.color.color_transparent_shift_focused)
        if (shiftKey) {
            var three = colorCapsOn
            colorCapsOn = colorCapsOff
            colorCapsOff = three
            three = focusedCapsOn
            focusedCapsOn = focusedCapsOff
            focusedCapsOff = three
        }
        val states = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_hovered),
            intArrayOf()
        )
        val colors1 = intArrayOf(
            focusedCapsOn,
            focusedCapsOn,
            focusedCapsOn,
            colorCapsOn
        )
        val colors2 = intArrayOf(
            focusedCapsOff,
            focusedCapsOff,
            focusedCapsOff,
            colorCapsOff
        )
        val myListColor1 = ColorStateList(states, colors1)
        val myListColor2 = ColorStateList(states, colors2)
        if (capsKey) {
            shiftText.setSpan(
                TextAppearanceSpan(null, 0, 0, myListColor2, null),
                0,
                2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            shiftText.setSpan(
                TextAppearanceSpan(null, 0, 0, myListColor1, null),
                0,
                1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            shiftText.setSpan(
                TextAppearanceSpan(null, 0, 0, myListColor2, null),
                1,
                2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        childButton?.text = shiftText
        childButton?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textDpSizeFull.toFloat())
    }

    private fun spannableTextAndColor(
        childButton: IconButton,
        chars: String?,
        textDpSize: Int,
        colorStateList: ColorStateList?
    ) {
        var btnText = chars
        val doubleSymbols: SpannableString
        btnText =
            if (shiftKey)
                btnText?.uppercase(Locale.getDefault())
            else
                btnText?.lowercase(Locale.getDefault())
        when (btnText?.lowercase(Locale.getDefault())) {
            "ї і",
            "і ї",
            "ґ г",
            "г ґ",
            "ё е",
            "е ё",
            "; :",
            ": ;",
            "/ \\",
            "\\ /" -> {
                doubleSymbols = SpannableString(btnText)
                setSpannableString(doubleSymbols, textDpSize, colorStateList, 3)
                childButton.text = doubleSymbols
            }
            "щш",
            "шщ",
            "ъь",
            "ьъ",
            ",.",
            ".," -> {
                doubleSymbols = SpannableString(btnText)
                setSpannableString(doubleSymbols, textDpSize, colorStateList, 2)
                childButton.text = doubleSymbols
            }
            LangSymbols.ABC,
            LangSymbols.ABV,
            LangSymbols.BUTTON_SYMB -> childButton.text = btnText
            LangSymbols.SPACE -> childButton.text = resources.getString(R.string.space)
            LangSymbols.DELETE -> childButton.text = resources.getString(R.string.delete)
            LangSymbols.CLEAR -> childButton.text = resources.getString(R.string.clear)
            LangSymbols.BACK -> childButton.text = resources.getString(R.string.previous)
            LangSymbols.NEXT -> childButton.text = resources.getString(R.string.next)
            else -> childButton.text = btnText?.substring(0, 1)
        }
        //button.setTextColor(myListColor);
    }

    private fun setSpannableString(
        btnText: SpannableString,
        textDpSize: Int,
        colorStateList: ColorStateList?,
        end: Int
    ) {
        btnText.setSpan(
            TextAppearanceSpan(null, 0, textDpSize, null, null),
            1,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        btnText.setSpan(SuperscriptSpan(), 1, end, 0)
        btnText.setSpan(RelativeSizeSpan(0.4f), 1, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        btnText.setSpan(
            TextAppearanceSpan(null, 0, 0, colorStateList, null),
            1,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun doubleClickButton(
        childButton: IconButton,
        btnText: String,
        textDpSize: Int
    ): Boolean {
        var secondText: String? = null
        doubleDouble = false
        when (btnText.lowercase(Locale.getDefault())) {
            ": ;" ->                 //case "; :":
                secondText = "; :"
            "і ї" ->                 //case "ї і":
                secondText = "ї і"
            "г ґ" ->                 //case "ґ г":
                secondText = "ґ г"
            "е ё" ->                 //case "ё е":
                secondText = "ё е"
            "/ \\" ->
                secondText = "\\ /"
            "шщ" ->                 //case "щш":
                secondText = "щш"
            "ьъ" ->                 //case "ъь":
                secondText = "ъь"
            ".," ->                 //case ",.":
                secondText = ",."
            "; :",
            "ї і",
            "ґ г",
            "ё е",
            "\\ /",
            "щш",
            "ъь",
            ",." ->
                doubleDouble = true
        }
        if (secondText != null || doubleDouble) {
            if (doubleDouble) {
                m += 2
                secondText = btnText
            } else
                m++
            if (m == 1) {
                secondPress = true
                firstButtonText = btnText
                trueKeyboard = keyboard
                trueSymbol = symbol
                handler = Handler()
                runnable = Runnable {
                    if (secondPress && m == 1) {
                        if (shiftKey) {
                            ic?.commitText(
                                firstButtonText!!.substring(0, 1).uppercase(Locale.getDefault()), 1
                            )
                            if (!capsKey)
                                mySetAllCaps(textDpSize)
                        } else {
                            ic?.commitText(firstButtonText!!.substring(0, 1), 1)
                        }
                    }
                    m = 0
                }
                handler?.postDelayed(runnable!!, 300)
            } else if (m == 2) {
                secondPress = false
                handler?.removeCallbacks(runnable!!)
                m = 0
                if (!doubleDouble) {
                    //doubleSecondText = btnText;
                    val doubleHandler = Handler()
                    val doubleRunnable = Runnable {
                        if (trueSymbol == symbol && trueKeyboard == keyboard) {
                            spannableTextAndColor(childButton, btnText, textDpSize, colorsSup)
                            //doubleDouble=false;
                        }
                    }
                    doubleHandler.postDelayed(doubleRunnable, 1000)
                    spannableTextAndColor(childButton, secondText, textDpSize, colorsSup)
                }
                if (shiftKey) {
                    ic?.commitText(secondText!!.substring(0, 1).uppercase(Locale.getDefault()), 1)
                    if (!capsKey)
                        mySetAllCaps(textDpSize)
                } else {
                    ic?.commitText(secondText!!.substring(0, 1), 1)
                }
            }
            return true
        }
        return btnText.length > 1
    }

    private fun largeButtonTextSize(textDpSizeFull: Int, vararg btnTextSize: IconButton?) {
        for (b in btnTextSize) {
            b?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textDpSizeFull.toFloat())
        }
    }

    private fun largeButtonTextBackground(vararg btnBackground: IconButton?) {
        for (b in btnBackground) {
            b?.setBackgroundResource(R.drawable.btn_shape_ftv_long)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        try {
            if (firstButtonText != null && m == 1 &&
                keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
                keyCode != KeyEvent.KEYCODE_NUMPAD_ENTER &&
                keyCode != KeyEvent.KEYCODE_ENTER
            ) {
                m = 0
                if (shiftKey || capsKey) {
                    ic?.commitText(
                        firstButtonText!!.substring(0, 1).uppercase(Locale.getDefault()),
                        1
                    )
                    //shiftKey=!shiftKey;
                    if (!capsKey)
                        mySetAllCaps(textDpSize)
                } else {
                    ic?.commitText(
                        firstButtonText!!.substring(0, 1).lowercase(Locale.getDefault()),
                        1
                    )
                }
            }
            if (isInputViewShown && kv!!.hasFocus() && kv != null) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        focusMove(View.FOCUS_LEFT, event)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        focusMove(View.FOCUS_RIGHT, event)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        focusMove(View.FOCUS_UP, event)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        focusMove(View.FOCUS_DOWN, event)
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        onKeyLongButton(btnDelete, event)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_ENTER -> {
                        focusButton = window.currentFocus as IconButton?
                        when (focusButton!!.id) {
                            btnDelete!!.id -> onKeyLongButton(btnDelete, event)
                            btnCursorLeft!!.id -> onKeyLongButton(btnCursorLeft, event)
                            btnCursorRight!!.id -> onKeyLongButton(btnCursorRight, event)
                            btnLang!!.id -> { // ADD LONG OK on btnLang
                                onKeyLongButton(btnLang, event)
                            }
                            else -> onKeyDownUp(focusButton, event, keyCode)
                        }
                        return true
                    }
                }
                if (event.repeatCount == 0) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_MEDIA_STOP -> {
                            onKeyDownUp(btnLang, event, keyCode)
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            onKeyDownUp(btnNext, event, keyCode)
                            return true
                        }
                        KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            onKeyDownUp(btnSpace, event, keyCode)
                            return true
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            onKeyDownUp(btnBack, event, keyCode)
                            return true
                        }
                    }
                } else { // ADD MENU LONG
                    if (keyCode == KeyEvent.KEYCODE_MENU && event.repeatCount % 4 == 0) {
                        startSettings()
                        return true
                    }
                }
            } else if (isInputViewShown && kv != null) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    btnBack?.requestFocus()
                    btnBack?.isPressed = true
                    btnBack?.invalidate()
                }
                return true
            }
            if (blockKey && (keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
            )
                return true
        } catch (e: Exception) {
        }
        return if (keyCode == KeyEvent.KEYCODE_ENTER) onKeyDown(
            KeyEvent.KEYCODE_DPAD_CENTER,
            event
        ) else
            super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (isInputViewShown && kv != null && kv!!.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_ENTER -> {
                    focusButton = window.currentFocus as IconButton?
                    onKeyDownUp(focusButton, event, keyCode)
                    return true
                }
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_MEDIA_STOP -> {
                    onKeyDownUp(btnLang, event, keyCode)
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    onKeyDownUp(btnNext, event, keyCode)
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                    onKeyDownUp(btnSpace, event, keyCode)
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    onKeyDownUp(btnDelete, event, keyCode)
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    onKeyDownUp(btnBack, event, keyCode)
                    return true
                }
            }
        } else if (isInputViewShown && kv != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                btnBack?.performClick()
            } else {
                kv?.findViewById<View>(firstFocus)?.requestFocusFromTouch()
            }
            return true
        }
        if (blockKey && (keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                    keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
        ) {
            blockKey = false
            return true
        }
        return if (keyCode == KeyEvent.KEYCODE_ENTER)
            onKeyUp(KeyEvent.KEYCODE_DPAD_CENTER, event)
        else
            super.onKeyUp(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (isInputViewShown && kv != null && kv!!.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    focusButton = window.currentFocus as IconButton?
                    if (focusButton!!.id == btnNext!!.id) {
                        handleClose()
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
                        blockKey = true
                    } else
                        btnShift?.performLongClick()
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    handleClose()
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
                }
            }
            return true
        }
        if (blockKey && (keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                    keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
        )
            return true
        return if (keyCode == KeyEvent.KEYCODE_ENTER) onKeyLongPress(
            KeyEvent.KEYCODE_DPAD_CENTER,
            event
        ) else
            super.onKeyLongPress(keyCode, event)
    }

    private fun focusMove(focus: Int, event: KeyEvent) {
        if (event.repeatCount % 2 == 0) {
            window.currentFocus?.focusSearch(focus)?.requestFocus()
        }
    }

    private fun onKeyLongButton(btn: IconButton?, event: KeyEvent) {
        if (event.repeatCount == 0) {
            btn?.isPressed = true
            btn?.invalidate()
        }
        if (event.repeatCount > 0) {
            if (btn!!.id == btnLang!!.id && event.repeatCount % 4 == 0) {
                startSettings()
            } else if (btn.id == btnCursorLeft!!.id || btn.id == btnCursorRight!!.id) {
                btn.performClick()
            } else if (event.repeatCount % 2 == 0) {
                btn.performClick()
            }
            if (event.repeatCount % 17 == 0) {
                if (btn.id == btnDelete!!.id)
                    deleteSpeed++
            }
        }
    }

    private fun onKeyDownUp(btn: IconButton?, event: KeyEvent, keyCode: Int) {
        if (event.action == KeyEvent.ACTION_DOWN) {
            event.startTracking()
            if (keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            ) {
                btn?.requestFocus()
            }
            btn?.isPressed = true
            btn?.invalidate()
        }
        if (btn != null && event.action == KeyEvent.ACTION_UP) {
            if (deleteSpeed != 1)
                deleteSpeed = 1
            btn.performClick()
            btn.isPressed = false
            btn.invalidate()
        }
    }

    private fun setImeOptions(res: Resources?, options: EditorInfo) {
        /*if (btnNext == null) {
            return;
        }
        switch (options.imeOptions&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
		//switch (options.imeOptions) {
			case EditorInfo.IME_ACTION_NEXT:
                //btnNext.setText(R.string.Next);
                //btnNext.invalidate();
                actionEnter=EditorInfo.IME_ACTION_NEXT;
                break;
            case EditorInfo.IME_ACTION_GO:
                //btnNext.setText(R.string.Go);
                //btnNext.invalidate();
                actionEnter=EditorInfo.IME_ACTION_GO;
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                //btnNext.setText(R.string.Search);
                //btnNext.invalidate();
                actionEnter=EditorInfo.IME_ACTION_SEARCH;
                break;
            case EditorInfo.IME_ACTION_SEND:
                //btnNext.setText(R.string.Send);
               // btnNext.invalidate();
                actionEnter=EditorInfo.IME_ACTION_SEND;
                break;
		    case EditorInfo.IME_ACTION_DONE:
				actionEnter=EditorInfo.IME_ACTION_DONE;
				break;
		    case EditorInfo.IME_ACTION_PREVIOUS:
				actionEnter=EditorInfo.IME_ACTION_PREVIOUS;
				break;
		    case 0:
                close=true;
				break;
            default:
                actionEnter=EditorInfo.IME_ACTION_NONE;;
                break;
        }*/
        inputType =
            options.inputType and (InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        actionEnter = options.imeOptions and EditorInfo.IME_MASK_ACTION
        close = actionEnter == 0
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        if (!isFullscreenMode) {
            outInsets.touchableInsets = outInsets.visibleTopInsets
        }
    }

    override fun onWindowShown() {
        setCandidatesViewShown(true)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return !close
    }

//    override fun onFinishInput() {
//        super.onFinishInput()
//    }

    private fun handleClose() {
        requestHideSelf(0)
    }

    private inner class IcWrapper(target: InputConnection?, mutable: Boolean) :
        InputConnectionWrapper(target, mutable) {
        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            val extractedText = getExtractedText(ExtractedTextRequest(), 0)
            val start = extractedText.selectionStart
            val end = extractedText.selectionEnd
            preview?.let {
                val p1 = it.text.subSequence(0, start)
                val p3 = it.text.subSequence(end, it.length())
                it.text = "$p1$text$p3"
                if (it.text.isNotEmpty())
                    it.visibility = View.VISIBLE
                else
                    it.visibility = View.INVISIBLE
            }
            return super.commitText(text, newCursorPosition)
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            val extractedText = getExtractedText(ExtractedTextRequest(), 0)
            val start = extractedText.selectionStart
            val end = extractedText.selectionEnd
            if (start - beforeLength < 0)
                return false
            preview?.let {
                val p1 = it.text.subSequence(0, start - beforeLength)
                val p2 = it.text.subSequence(start, end)
                val p3 = it.text.subSequence(end + afterLength, it.length())
                it.text = "$p1$p2$p3"
                if (it.text.isNotEmpty())
                    it.visibility = View.VISIBLE
                else
                    it.visibility = View.INVISIBLE

                it.postInvalidate()
            }
            return super.deleteSurroundingText(beforeLength, afterLength)
        }

        override fun getExtractedText(request: ExtractedTextRequest, flags: Int): ExtractedText {
            val extractedText = super.getExtractedText(request, flags)
            preview?.let {
                it.text = extractedText.text
                if (it.text.isNotEmpty())
                    it.visibility = View.VISIBLE
                else
                    it.visibility = View.INVISIBLE
            }
            return extractedText
        }
    }
}