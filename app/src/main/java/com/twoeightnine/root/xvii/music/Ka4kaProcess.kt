package com.twoeightnine.root.xvii.music


fun getSearchData(site: String): Array<Int> {
    val pattern = Regex("""id=[0-9]{5,7}&amp;r=[0-9]{8,12}""")
    val result = pattern.find(site)
    val str = result?.value ?: ""
    val id = Regex("""[0-9]{5,7}""").find(str)?.value?.toInt() ?: 0
    val r = Regex("""[0-9]{8,12}""").find(str)?.value?.toInt() ?: 0
    return arrayOf(id, r)
}

fun getTracks(site: String): MutableList<Track> {
    val result: MutableList<Track> = mutableListOf()
    val pattern = Regex("""mod=content&amp;one=[0-9]{8,11}&amp;two=[0-9]{8,11}&amp;three=[0-9a-f]{16,20}//[0-9a-f]{16,20}&amp;idcode=[0-9]{5,7}&amp;s=[0-9]{1,2}">[0-9A-Za-zА-ЯЁа-яё -]*<span style="color:#000;">\([0-9]{1,3} """)
    val matches = pattern.findAll(site)
    matches.forEach {
        val str = it.value
        val one = Regex("""one=[0-9]{8,11}""")
                .find(str)
                ?.value
                ?.substring(4)
                ?.toInt() ?: 0

        val two = Regex("""two=[0-9]{8,11}""")
                .find(str)
                ?.value
                ?.substring(4)
                ?.toInt() ?: 0

        val three = Regex("""three=[0-9a-f]{16,20}//[0-9a-f]{16,20}""")
                .find(str)
                ?.value
                ?.substring(6) ?: ""

        val idCode = Regex("""idcode=[0-9]{5,7}""")
                .find(str)
                ?.value
                ?.substring(7)
                ?.toInt() ?: 0

        val s = Regex("""s=[0-9]{1,2}""")
                .find(str)
                ?.value
                ?.substring(2)
                ?.toInt() ?: 0

        val artistTitle = Regex(""">[0-9A-Za-zА-ЯЁа-яё -]*<""")
                .find(str)
                ?.value
                ?.substring(1)
                ?.split(" - ") ?: listOf()

        val artist = artistTitle[0]
        val title = artistTitle[1].substring(0, artistTitle[1].length - 2)
        val duration = Regex(""">\([0-9]{1,3}""")
                .find(str)
                ?.value
                ?.substring(2)
                ?.toInt() ?: 0

        result.add(Track(artist, title, duration, null, TrackUrl(
                one, two, three, idCode, s
        )))
    }
    return result
}

fun getTrackId(site: String): Int {
    val numStr = Regex(""" \([0-9]{7,10}\)\.""")
            .find(site)
            ?.value ?: ""
    return numStr.substring(2, numStr.length - 2)
            .toInt()
}