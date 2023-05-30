package com.example.myapplication

const val START_GAME_SPEED = 500L
const val MINIMUM_GAME_SPEED = 200L

object SnakeCore {
    var nextMove: () -> Unit = {}
    var isPlay = true
    var gameSpeed = START_GAME_SPEED

    init {
        Thread {
            while (true) {
                Thread.sleep(gameSpeed)
                if (isPlay) {
                    nextMove()
                }
            }
        }.start()
    }

    fun startTheGame() {
        gameSpeed = START_GAME_SPEED
        isPlay = true
    }
}

