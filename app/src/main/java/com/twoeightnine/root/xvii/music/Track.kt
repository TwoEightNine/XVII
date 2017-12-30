package com.twoeightnine.root.xvii.music

data class Track(
        var artist: String,
        var title: String,
        var duration: Int,
        var url: String?,
        var preUrl: TrackUrl
)