package com.anhq.smartalarm.core.model

data class AlarmGame(
    val gameName: NameOfGame,
    val gameId: Int
) {
    companion object {
        val defaultGames = NameOfGame.entries.map {
            AlarmGame(it, 0)
        }
    }
}

enum class NameOfGame(val label: String) {
    SUN("None"),
    MON("QR Scan"),
    TUE("Math Challenge"),
    WED("Shake to Wake")
}