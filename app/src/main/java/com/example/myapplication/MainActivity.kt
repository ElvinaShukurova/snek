package com.example.myapplication

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.SnakeCore.gameSpeed
import com.example.myapplication.SnakeCore.isPlay
import com.example.myapplication.SnakeCore.startTheGame
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    private val allTale = mutableListOf<PartOfTale>()
    private var currentDirection: Directions = Directions.BOTTOM
    private var HEAD_SIZE = -1
    private val CELLS_ON_FIELD_HORIZONTALLY = 11
    private val CELLS_ON_FIELD_VERTICALLY = 16

    private val human by lazy {
        ImageView(this).apply {
            this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
            this.setImageResource(R.drawable.baseline_person)
        }
    }
    private val head by lazy {
        ImageView(this)
            .apply {
                this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
                this.setImageResource(R.drawable.snake_head)
            }
    }
    private lateinit var container: FrameLayout

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


        startTheGame()
        generateNewHuman()
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
            if (isPlay) {
                ivPause.setImageResource(R.drawable.baseline_play)
            } else {
                ivPause.setImageResource(R.drawable.baseline_pause)
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

    private fun generateNewHuman() {
        val viewCoordinate = generateHumanCoordinates()
        (human.layoutParams as FrameLayout.LayoutParams).topMargin = viewCoordinate.top
        (human.layoutParams as FrameLayout.LayoutParams).leftMargin = viewCoordinate.left
        container.removeView(human)
        container.addView(human)
    }

    private fun generateHumanCoordinates(): ViewCoordinate {
        val viewCoordinate = ViewCoordinate(
            (0 until CELLS_ON_FIELD_VERTICALLY).random() * HEAD_SIZE,
            (0 until CELLS_ON_FIELD_HORIZONTALLY).random() * HEAD_SIZE
        )
        for (partTale in allTale) {
            if (partTale.viewCoordinate == viewCoordinate) {
                return generateHumanCoordinates()
            }
        }
        if (head.top == viewCoordinate.top && head.left == viewCoordinate.left) {
            return generateHumanCoordinates()
        }
        return viewCoordinate
    }

    private fun checkIfSnakeEatsPerson() {
        if (head.left == human.left && head.top == human.top) {
            generateNewHuman()
            addPartOfTale(head.top, head.left)
            increaseDifficult()
        }
    }

    private fun increaseDifficult() {
        if (gameSpeed <= MINIMUM_GAME_SPEED) {
            return
        }
        if (allTale.size % 5 == 0) {
            gameSpeed -= 100
        }
    }

    private fun addPartOfTale(top: Int, left: Int) {
        val talePart = drawPartOfTale(top, left)
        allTale.add(PartOfTale(ViewCoordinate(top, left), talePart))
    }

    private fun drawPartOfTale(top: Int, left: Int): ImageView {
        val taleImage = ImageView(this)
        taleImage.setImageResource(R.drawable.snake_scales)
        taleImage.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
        (taleImage.layoutParams as FrameLayout.LayoutParams).topMargin = top
        (taleImage.layoutParams as FrameLayout.LayoutParams).leftMargin = left

        container.addView(taleImage)
        return taleImage
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
            makeTaleMove()
            checkIfSnakeEatsPerson()
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
            .setTitle("Your score: ${allTale.size} items LOSER")
            .setPositiveButton("OK") { _, _ ->
                this.recreate()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun checkIfSnekDed(): Boolean {
        for (talePart in allTale) {
            if (talePart.viewCoordinate.left == head.left && talePart.viewCoordinate.top == head.top) {
                return true
            }
        }
        if (head.top < 0
            || head.left < 0
            || head.top >= HEAD_SIZE * CELLS_ON_FIELD_VERTICALLY
            || head.left >= HEAD_SIZE * CELLS_ON_FIELD_HORIZONTALLY
        ) {
            return true
        }
        return false
    }

    private fun makeTaleMove() {
        var tempTalePart: PartOfTale? = null
        for (index in 0 until allTale.size) {
            val talePart = allTale[index]
            container.removeView(talePart.imageView)
            if (index == 0) {
                tempTalePart = talePart
                allTale[index] = PartOfTale(ViewCoordinate(head.top, head.left), drawPartOfTale(head.top, head.left))
            } else {
                val anotherTempPartOfTale = allTale[index]
                tempTalePart?.let {
                    allTale[index] =
                        PartOfTale(it.viewCoordinate, drawPartOfTale(it.viewCoordinate.top, it.viewCoordinate.left))
                }
                tempTalePart = anotherTempPartOfTale
            }
        }
    }
}

enum class Directions {
    UP,
    RIGHT,
    BOTTOM,
    LEFT
}


data class ViewCoordinate(
    val top: Int,
    val left: Int
    )