package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.system.measureNanoTime


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    //val field = List(10) { MutableList(10) { "Cell" } }
    val curCoords = Coordinate(0, 0)
    val curDirection = Directions.BOTTOM
    private fun move(direction: Directions) {
        when (direction) {
            Directions.UP -> {
                curCoords.top--
            }

            Directions.BOTTOM -> {
                curCoords.top++
            }

            Directions.LEFT -> {
                curCoords.left--
            }

            Directions.RIGHT -> {
                curCoords.left++
            }
        }
        println(curCoords)
    }

    @Test
    fun `Check if 9 moves are done by 4,5 +- 0,1 seconds`() {
        SnakeCore.nextMove = { move(curDirection) }
        assertEquals(
            4.5e9,

            measureNanoTime {
                while (true) {
                    if (curCoords.top == 9) break
                    Thread.sleep(100)
                }
            }.toDouble(),

            1e8
        )
    }
}