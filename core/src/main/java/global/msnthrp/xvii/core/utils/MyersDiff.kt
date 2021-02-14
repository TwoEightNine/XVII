package global.msnthrp.xvii.core.utils

object MyersDiff {


    /**
     * calculates difference using [splitByWordsAndSigns]
     */
    fun getDiffByWordsAndSigns(a: String, b: String): List<Change<String>> {
        return getDiff(a.splitByWordsAndSigns(), b.splitByWordsAndSigns())
    }

    /**
     * calculates difference using [split] by space,
     * kid of split by words but without any respect to signs
     */
    fun getDiffByWords(a: String, b: String): List<Change<String>> {
        return getDiff(a.split(" "), b.split(" "))
    }

    /**
     * calculates difference by single character
     */
    fun getDiffByCharacters(a: String, b: String): List<Change<Char>> {
        return getDiff(a.toList(), b.toList())
    }

    /**
     * splits string into words and signs:
     * signs are represented as a separated word, words are followed by space
     */
    private fun String.splitByWordsAndSigns(): List<String> {
        val signs = ".,<>/?!\"\n:;'()*"
        val result = arrayListOf<String>()
        try {
            var startPos = 0
            var pointer = 0
            var isWord = this[pointer] !in signs
            while (pointer < length - 1) {
                pointer++
                val isWordNow = this[pointer] !in signs
                val isSpaceNow = this[pointer] == ' '
                if (isWord != isWordNow || !isWord || isSpaceNow) {
                    isWord = isWordNow
                    result.add(substring(startPos, pointer))
                    startPos = pointer
                }
            }
            result.add(substring(startPos, pointer + 1))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return result
        }
    }

    /**
     * core algo that implements myers diff algorithm
     */
    private fun <T : Any> getDiff(a: List<T>, b: List<T>): List<Change<T>> {
        val frontier = hashMapOf<Int, Frontier<T>>()
        frontier[1] = Frontier(0, emptyList())

        val aMax = a.size
        val bMax = b.size

        for (d in 0..(aMax + bMax)) {
            for (k in -d..d step 2) {
                val goDown = k == -d || (k != d && frontier[k - 1]!!.x < frontier[k + 1]!!.x)

                var oldX: Int
                var history: ArrayList<Change<T>>
                var x: Int
                if (goDown) {
                    frontier[k + 1]!!.also { fr ->
                        oldX = fr.x
                        history = ArrayList(fr.history)
                        x = oldX
                    }
                } else {
                    frontier[k - 1]!!.also { fr ->
                        oldX = fr.x
                        history = ArrayList(fr.history)
                        x = oldX + 1
                    }
                }

                history = ArrayList(history)

                var y = x - k
                if (y in 1..bMax && goDown) {
                    history.add(Change.Insert(b[y - 1]))
                } else if (x in 1..aMax) {
                    history.add(Change.Remove(a[x - 1]))
                }

                while (x < aMax && y < bMax && a[x] == b[y]) {
                    x++; y++
                    history.add(Change.Keep(a[x - 1]))
                }

                if (x >= aMax && y >= bMax) {
                    return history
                } else {
                    frontier[k] = Frontier(x, history)
                }
            }
        }

        return emptyList()
    }

    data class Frontier<T : Any>(
            val x: Int,
            val history: List<Change<T>>
    )

    /**
     * change betwee two sentences
     */
    sealed class Change<T : Any>(val elem: T) {

        /**
         * [elem] is kept
         */
        class Keep<T : Any>(elem: T) : Change<T>(elem)

        /**
         * [elem] is inserted
         */
        class Insert<T : Any>(elem: T) : Change<T>(elem)

        /**
         * [elem is removed]
         */
        class Remove<T : Any>(elem: T) : Change<T>(elem)
    }
}