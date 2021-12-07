package com.amazon.tv.ftvstylekb

/**
 * Created by semitop7 on 10.03.16.
 */
object LangSymbols {
    const val ABV = "абв"
    const val ABC = "abc"
    const val А_А = "аА"
    const val BUTTON_SYMB = "#$%"
    const val SPACE = "space"
    const val DELETE = "delete"
    const val CLEAR = "clear"
    const val BACK = "back"
    const val LEFT = "◀"
    const val RIGHT = "▶"
    const val NEXT = "next"

    @JvmField
    val KEY_RU_ABV = arrayOf(
            "а", "б", "в", "г ґ", "д", "е ё", "ж", "з", "и", "й",
            "к", "л", "м", "н", "о", "п", "р", "с", "т", "у",
            "ф", "х", "ц", "ч", "шщ", "ы", "ьъ", "э", "ю", "я",
            "є", "і ї", ".", ",", ":", ";", "-", "_", "!", "?",
            ABC, А_А, BUTTON_SYMB, SPACE, DELETE, CLEAR,
            BACK, LEFT, RIGHT, NEXT)

    @JvmField
    val KEY_RU_YCU = arrayOf(
            "й", "ц", "у", "к", "е ё", "н", "г ґ", "шщ", "з", "х",
            "ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж",
            "я", "ч", "с", "м", "и", "т", "ьъ", "б", "ю", "э",
            "є", "і ї", ".", ",", ":", ";", "-", "_", "!", "?",
            ABC, А_А, BUTTON_SYMB, SPACE, DELETE, CLEAR,
            BACK, LEFT, RIGHT, NEXT)

    @JvmField
    val KEY_EN_ABC = arrayOf(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", ".,", ": ;", "/ \\", "@",
            ABV, А_А, BUTTON_SYMB, SPACE, DELETE, CLEAR,
            BACK, LEFT, RIGHT, NEXT)

    @JvmField
    val KEY_EN_QWE = arrayOf(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l", "/ \\",
            "z", "x", "c", "v", "b", "n", "m", ".,", ": ;", "@",
            ABV, А_А, BUTTON_SYMB, SPACE, DELETE, CLEAR,
            BACK, LEFT, RIGHT, NEXT)

    @JvmField
    val SYMBOLS = arrayOf(
            "~", "`", "•", "™", "©", "°", "¢", "®", "«", "»",
            "&", "*", "\"", "'", "=", "_", "(", ")", "[", "]",
            ":", ";", "^", "/", "|", "\\", "{", "}", "<", ">",
            "+", "-", "#", "$", "%", "?", "¿", "¡", "£", "€")
}