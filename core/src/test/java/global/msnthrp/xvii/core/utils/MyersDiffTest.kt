package global.msnthrp.xvii.core.utils

import org.junit.Assert
import org.junit.Test

class MyersDiffTest {

    @Test
    fun correctness() {

        // the same
        val diff1 = MyersDiff.getDiffByCharacters(TEXT_1_1, TEXT_1_1)
        val inserts1 = diff1.inserts()
        val removes1 = diff1.removes()

        Assert.assertTrue(inserts1.isEmpty())
        Assert.assertTrue(removes1.isEmpty())
        printDiff(diff1)

        // insert one char
        val diff2 = MyersDiff.getDiffByCharacters(TEXT_1_1, TEXT_1_2)
        val inserts2 = diff2.inserts()
        val removes2 = diff2.removes()

        Assert.assertEquals(1, inserts2.size)
        Assert.assertEquals(0, removes2.size)
        val insert2 = inserts2.first()
        Assert.assertEquals('w', insert2.elem)
        Assert.assertEquals('s', diff2.previousOf(insert2)?.elem)
        Assert.assertEquals('e', diff2.nextOf(insert2)?.elem)
        printDiff(diff2)

        // remove one char
        val diff3 = MyersDiff.getDiffByCharacters(TEXT_1_2, TEXT_1_1)
        val inserts3 = diff3.inserts()
        val removes3 = diff3.removes()

        Assert.assertEquals(0, inserts3.size)
        Assert.assertEquals(1, removes3.size)
        val remove3 = removes3.first()
        Assert.assertEquals('w', remove3.elem)
        Assert.assertEquals('s', diff3.previousOf(remove3)?.elem)
        Assert.assertEquals('e', diff3.nextOf(remove3)?.elem)
        printDiff(diff3)

        // remove whole sentence
        val diff4 = MyersDiff.getDiffByCharacters(TEXT_2_1, TEXT_2_2)
        val inserts4 = diff4.inserts()
        val removes4 = diff4.removes()

        Assert.assertEquals(0, inserts4.size)
        Assert.assertEquals(38, removes4.size)
        printDiff(diff4)

        // insert whole sentence
        val diff5 = MyersDiff.getDiffByCharacters(TEXT_2_2, TEXT_2_1)
        val inserts5 = diff5.inserts()
        val removes5 = diff5.removes()

        Assert.assertEquals(38, inserts5.size)
        Assert.assertEquals(0, removes5.size)
        printDiff(diff5)

        // insert whole sentence
        val diff6 = MyersDiff.getDiffByCharacters(TEXT_3_1, TEXT_3_2)
        val inserts6 = diff6.inserts()
        val removes6 = diff6.removes()

        Assert.assertEquals(25, inserts6.size)
        Assert.assertEquals(25, removes6.size)
        printDiff(diff6)
    }

    @Test
    fun speed() {
        runWithTime("text_1") {
            MyersDiff.getDiffByCharacters(TEXT_1_1, TEXT_1_2)
        }
        runWithTime("text_3") {
            MyersDiff.getDiffByCharacters(TEXT_3_1, TEXT_3_2)
        }
        val finalAvgTime = runWithTime("text_4") {
            MyersDiff.getDiffByCharacters(TEXT_4_1, TEXT_4_2)
        }
        Assert.assertTrue(finalAvgTime < 100)
    }

    private fun <T : Any> List<MyersDiff.Change<T>>.keeps() = filterIsInstance<MyersDiff.Change.Keep<T>>()
    private fun <T : Any> List<MyersDiff.Change<T>>.inserts() = filterIsInstance<MyersDiff.Change.Insert<T>>()
    private fun <T : Any> List<MyersDiff.Change<T>>.removes() = filterIsInstance<MyersDiff.Change.Remove<T>>()

    private fun <T : Any> List<T>.previousOf(item: T) = getOrNull(indexOf(item) - 1)
    private fun <T : Any> List<T>.nextOf(item: T) = getOrNull(indexOf(item) + 1)

    private fun runWithTime(tag: String? = null, times: Int = 100, runnable: () -> Unit): Long {
        var avgMs = 0L
        for (i in 0 until times) {
            val start = System.currentTimeMillis()
            runnable()
            avgMs += (System.currentTimeMillis() - start)
        }
        avgMs /= times
        tag?.also {
            println("$tag took $avgMs ms to run")
        }
        return avgMs
    }

    private fun printDiff(diff: List<MyersDiff.Change<Char>>) {
        if (!PRINT_DIFF) return

        for (change in diff) {
            when (change) {
                is MyersDiff.Change.Keep -> print(change.elem)
                is MyersDiff.Change.Insert -> print("+${change.elem}")
                is MyersDiff.Change.Remove -> print("-${change.elem}")
            }
        }
        println()
    }

    companion object {

        private const val PRINT_DIFF = true

        // insert w between s and e
        private const val TEXT_1_1 = "This may not give the correct anser. According to the docs, \"Note that even though the time is always returned as a floating point number, not all systems provide time with a better precision than 1 second\""
        private const val TEXT_1_2 = "This may not give the correct answer. According to the docs, \"Note that even though the time is always returned as a floating point number, not all systems provide time with a better precision than 1 second\""

        // remove the first sentence
        private const val TEXT_2_1 = "This may not give the correct answer. According to the docs, \"Note that even though the time is always returned as a floating point number, not all systems provide time with a better precision than 1 second\""
        private const val TEXT_2_2 = "According to the docs, \"Note that even though the time is always returned as a floating point number, not all systems provide time with a better precision than 1 second\""

        // partially move first sentence (remove and insert)
        private const val TEXT_3_1 = "This may not give the correct answer. According to the docs, \"Note that even though the time is always returned as a floating point number, not all systems provide time with a better precision than 1 second\""
        private const val TEXT_3_2 = "According to the docs, this may not give the correct answer: \"Note that even though the time is always returned as a floating point number, not all systems provide time with a better precision than 1 second\""

        // large text for speed test
        private const val TEXT_4_1 = "These multiplications to 1000 for milliseconds may be decent for solving or making some prerequisite acceptable. Although, for real situations which require precise timing it would ultimately fail. I wouldn't suggest anyone use this method for mission-critical operations which require actions, or processing at specific timings.\n" +
                "For example: round-trip pings being 30-80ms in the USA... You couldn't just round that up and use it efficiently.\n" +
                "My own example requires tasks at every second which means if I rounded up after the first tasks responded I would still incur the processing time multiplied every main loop cycle. This ended up being a total function call every 60 seconds. that's ~1440 a day.. not too accurate.\n" +
                "Just a thought for people looking for more accurate reasoning beyond solving a database gap which never really uses it."
        private const val TEXT_4_2 = "These multiplications to 1000 for milliseconds may be decent for solving or making some prerequisite acceptable. It could be used to fill a gap in your database which doesn't really ever use it. Although, for real situations which require precise timing it would ultimately fail. I wouldn't suggest anyone use this method for mission-critical operations which require actions, or processing at specific timings.\n" +
                "My own example requires tasks at every second which means if I rounded up after the first tasks responded I would still incur the processing time multiplied every main loop cycle. This ended up being a total function call every 60 seconds. that's ~1440 a day.. not too accurate.\n" +
                "Just a thought for people looking for more accurate reasoning beyond solving a database gap which never really uses it."
    }
}