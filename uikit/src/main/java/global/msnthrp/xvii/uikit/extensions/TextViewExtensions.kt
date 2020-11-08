package global.msnthrp.xvii.uikit.extensions

import android.widget.EditText
import android.widget.TextView
import java.util.*


fun TextView.lowerIf(lower: Boolean) {
    if (lower) lower()
}

fun TextView.lower() {
    text = text.toString().toLowerCase(Locale.ROOT)
}

fun EditText.asText() = text.toString()

fun EditText.clear() {
    setText("")
}
