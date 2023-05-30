package com.example.myapplication

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.SnakeCore.gameSpeed
import com.example.myapplication.SnakeCore.isPlay
import com.example.myapplication.SnakeCore.startTheGame
import java.io.File
import kotlin.math.ceil
import kotlin.random.Random

private var bestScore = 0

class MainActivity : AppCompatActivity() {

    private val allTail = mutableListOf<PartOfTail>()
    private var currentDirection: Directions = Directions.BOTTOM
    private var HEAD_SIZE = -1
    private val CELLS_ON_FIELD_HORIZONTALLY = 11
    private val CELLS_ON_FIELD_VERTICALLY = 16
    private lateinit var bestScoreFile: File
    private val wallsCoordinate = mutableSetOf<Coordinate>()
    private lateinit var container: FrameLayout

    private val heart by lazy {
        ImageView(this).apply {
            this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
            this.setImageResource(R.drawable.heart)
        }
    }

    private val head by lazy {
        ImageView(this).apply {
            this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
            this.setImageResource(R.drawable.snake_head)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HEAD_SIZE =
            ceil(
            applicationContext.resources.displayMetrics.widthPixels.toDouble() /
                    CELLS_ON_FIELD_HORIZONTALLY
            ).toInt()

        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        container.layoutParams = LinearLayout.LayoutParams(
            HEAD_SIZE * CELLS_ON_FIELD_HORIZONTALLY,
            HEAD_SIZE * CELLS_ON_FIELD_VERTICALLY
        )

        val ivArrowUp = findViewById<ImageView>(R.id.ivArrowUp)
        val ivArrowBottom = findViewById<ImageView>(R.id.ivArrowBottom)
        val ivArrowRight = findViewById<ImageView>(R.id.ivArrowRight)
        val ivArrowLeft = findViewById<ImageView>(R.id.ivArrowLeft)
        val ivPause = findViewById<ImageView>(R.id.ivPause)
        val best = findViewById<TextView>(R.id.best)

        bestScoreFile = File(applicationContext.filesDir, "bestScore")
        if (bestScoreFile.createNewFile()) bestScoreFile.writeText(bestScore.toString())
        bestScore = bestScoreFile.readText().toInt()
        best.text = " BEST: $bestScore"


        startTheGame()
        generateNewHeart()
        SnakeCore.nextMove = { move(Directions.BOTTOM) }



        ivArrowUp.setOnClickListener {
            SnakeCore.nextMove = { checkIfCurrentDirectionIsNotOpposite(Directions.UP, Directions.BOTTOM) }
        }
        ivArrowBottom.setOnClickListener {
            SnakeCore.nextMove = { checkIfCurrentDirectionIsNotOpposite(Directions.BOTTOM, Directions.UP) }
        }
        ivArrowLeft.setOnClickListener {
            SnakeCore.nextMove = { checkIfCurrentDirectionIsNotOpposite(Directions.LEFT, Directions.RIGHT) }
        }
        ivArrowRight.setOnClickListener {
            SnakeCore.nextMove = { checkIfCurrentDirectionIsNotOpposite(Directions.RIGHT, Directions.LEFT) }
        }
        ivPause.setOnClickListener {
            if (!isPlay) {
                ivPause.setImageResource(R.drawable.baseline_pause)
            } else {
                ivPause.setImageResource(R.drawable.baseline_play)
            }
            isPlay = !isPlay
        }
    }

    private fun checkIfCurrentDirectionIsNotOpposite(rightDirection: Directions, oppositeDirection: Directions) {
        if (currentDirection == oppositeDirection) {
            move(currentDirection)
        } else {
            move(rightDirection)
        }
    }

    private fun generateNewHeart() {
        val viewCoordinate = generateHeartCoordinates()
        (heart.layoutParams as FrameLayout.LayoutParams).topMargin = viewCoordinate.top
        (heart.layoutParams as FrameLayout.LayoutParams).leftMargin = viewCoordinate.left
        container.removeView(heart)
        container.addView(heart)
    }

    private fun generateHeartCoordinates(): Coordinate {
        val coordinate = Coordinate(
            (0 until CELLS_ON_FIELD_VERTICALLY).random() * HEAD_SIZE,
            (0 until CELLS_ON_FIELD_HORIZONTALLY).random() * HEAD_SIZE
        )
        for (partTail in allTail) {
            if (partTail.coordinate == coordinate) {
                return generateHeartCoordinates()
            }
        }
        if (coordinate in wallsCoordinate) {
            return generateHeartCoordinates()
        }
        if (head.top == coordinate.top && head.left == coordinate.left) {
            return generateHeartCoordinates()
        }
        return coordinate
    }

    private fun checkIfSnakeEatsHeart() {
        if (head.left == heart.left && head.top == heart.top) {
            addPartOfTail(head.top, head.left)
            increaseDifficult()
            generateNewHeart()
            val score = findViewById<TextView>(R.id.score)
            val best = findViewById<TextView>(R.id.best)
            score.text = " SCORE: ${allTail.size}"
            if (bestScore < allTail.size) {
                bestScore = allTail.size
                best.text = " BEST: $bestScore"
                bestScoreFile.writeText(bestScore.toString())
            }


        }
    }

    private fun increaseDifficult() {
        if (gameSpeed <= MINIMUM_GAME_SPEED) {
            return
        }
        if (allTail.size % 5 == 0) {
            gameSpeed -= 100
        }
        val left = Random.nextInt(CELLS_ON_FIELD_HORIZONTALLY) * HEAD_SIZE
        val top = Random.nextInt(CELLS_ON_FIELD_VERTICALLY) * HEAD_SIZE
        var f = true
        for (tailPart in allTail) {
            if (tailPart.coordinate.top == top
                && tailPart.coordinate.left == left
            ) f = false
        }
        if (f) {
            wallsCoordinate.add(Coordinate(top, left))
            container.addView(ImageView(this).apply {
                this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
                this.y = top.toFloat()
                this.x = left.toFloat()
                this.setImageResource(R.drawable.mushroom)
            })
        }
    }

    private fun addPartOfTail(top: Int, left: Int) {
        val tailPart = drawPartOfTail(top, left)
        allTail.add(PartOfTail(Coordinate(top, left), tailPart))
    }

    private fun drawPartOfTail(top: Int, left: Int): ImageView {
        val tailImage = ImageView(this)
        tailImage.setImageResource(R.drawable.snake_scales)
        tailImage.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
        (tailImage.layoutParams as FrameLayout.LayoutParams).topMargin = top
        (tailImage.layoutParams as FrameLayout.LayoutParams).leftMargin = left

        container.addView(tailImage)
        return tailImage
    }

    private fun move(direction: Directions) {
        when (direction) {
            Directions.UP -> {
                moveHeadAndRotate(Directions.UP, 90f, -HEAD_SIZE)
            }
            Directions.BOTTOM -> {
                moveHeadAndRotate(Directions.BOTTOM, 270f, HEAD_SIZE)
            }
            Directions.LEFT -> {
                moveHeadAndRotate(Directions.LEFT, 0f, -HEAD_SIZE)
            }
            Directions.RIGHT -> {
                moveHeadAndRotate(Directions.RIGHT, 180f, HEAD_SIZE)
            }
        }
        runOnUiThread {
            if (checkIfSnekDed()) {
                isPlay = false
                showScore()
                return@runOnUiThread
            }
            makeTailMove()
            checkIfSnakeEatsHeart()
            container.removeView(head)
            container.addView(head)
        }
    }

    private fun moveHeadAndRotate(direction: Directions, angle: Float, coordinates: Int) {
        head.rotation = angle
        when (direction) {
            Directions.UP, Directions.BOTTOM -> {
                (head.layoutParams as FrameLayout.LayoutParams).topMargin += coordinates
            }
            Directions.LEFT, Directions.RIGHT -> {
                (head.layoutParams as FrameLayout.LayoutParams).leftMargin += coordinates
            }
        }
        currentDirection = direction
    }

    private fun showScore() {
        AlertDialog.Builder(this)
            .setTitle("Your score: ${allTail.size} items. For a better result, give Elvina a good grade")
            .setPositiveButton("OK") { _, _ ->
                this.recreate()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun checkIfSnekDed(): Boolean {
        for (tailPart in allTail) {
            if (tailPart.coordinate.left == head.left && tailPart.coordinate.top == head.top) {
                return true
            }
        }
        if (head.top < 0
            || head.left < 0
            || head.top >= HEAD_SIZE * CELLS_ON_FIELD_VERTICALLY
            || head.left >= HEAD_SIZE * CELLS_ON_FIELD_HORIZONTALLY
            || Coordinate(head.top, head.left) in wallsCoordinate
        ) {
            return true
        }
        return false
    }

    private fun makeTailMove() {
        var tempTailPart: PartOfTail? = null

        for (index in 0 until allTail.size) {
            val tailPart = allTail[index]
            container.removeView(tailPart.imageView)
            if (index == 0) {
                tempTailPart = tailPart
                allTail[index] = PartOfTail(Coordinate(head.top, head.left), drawPartOfTail(head.top, head.left))
            } else {
                val anotherTempPartOfTail = allTail[index]
                tempTailPart?.let {
                    allTail[index] =
                        PartOfTail(it.coordinate, drawPartOfTail(it.coordinate.top, it.coordinate.left))
                }
                tempTailPart = anotherTempPartOfTail
            }
        }
    }
}

