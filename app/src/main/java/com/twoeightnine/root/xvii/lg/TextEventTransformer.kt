package com.twoeightnine.root.xvii.lg

import com.twoeightnine.root.xvii.utils.getTime

class TextEventTransformer : L.EventTransformer {

    override fun transform(event: LgEvent): String {
        val sb = StringBuilder()
        sb.append(getTime(event.ts, withSeconds = true))
                .append(' ')
        event.tag?.also { sb.append("[$it] ") }
        sb.append(event.text)
        event.throwable?.also { sb.append("\nthrown: ${it.message}") }
        return sb.toString()
    }
}