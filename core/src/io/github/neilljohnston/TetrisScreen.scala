package io.github.neilljohnston

import com.badlogic.gdx.{Gdx, ScreenAdapter}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera, Texture}
import com.badlogic.gdx.scenes.scene2d
import com.badlogic.gdx.graphics.g2d.{NinePatch, TextureRegion}
import com.badlogic.gdx.utils.viewport.{FitViewport, ScreenViewport}
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.TimeUtils

import scala.collection.mutable

class TetrisScreen(game: TetrisScala) extends ScreenAdapter {
    // Init camera, viewport, random generator
    val camera = new OrthographicCamera()
    camera.setToOrtho(false, 320, 320)
    val viewport = new FitViewport(320, 320, camera)
    val stageViewport = new ScreenViewport()
    val random: RandomXS128 = new RandomXS128()

    // Init textures, texture regions
    val minoTexture = new Texture(Gdx.files.internal("mino.png"))
    val minoTypes: IndexedSeq[TextureRegion] =
        for(i <- 0 to 7) yield new TextureRegion(minoTexture, 0, 16*i, 16, 16)
    val ghostTexture = new Texture(Gdx.files.internal("ghost.png"))
    val ghostTypes: IndexedSeq[TextureRegion] =
        for(i <- 0 to 7) yield new TextureRegion(ghostTexture, 0, 16*i, 16, 16)

    // Init game field, player, Tetromino queue, Tetromino memory (for random Tetromino dist.), score
    var field: Array[Array[Int]] = Array.ofDim[Int](24, 10)
    var player: Tetromino = randomTetromino()
    val preview: mutable.Queue[Tetromino] = new mutable.Queue[Tetromino]() +=
        randomTetromino() += randomTetromino() += randomTetromino()
    val recent: mutable.Queue[Int] = new mutable.Queue[Int]() += player.minoType
    var score: Int = 0

    // Timing constants
    final val INITIAL_DELAY: Long = 150
    final val REPEAT_DELAY: Long = 50
    final val LIFE: Long = 5000
    final val GRAVITY: Long = 1000
    final val LIFE_ACCEL: Double = 0.5
    final val GRAVITY_ACCEL: Double = 0.5

    // Scheduling variables
    var scheduleLeft: Long = 0
    var scheduleRight: Long = 0
    var scheduleDown: Long = 0
    var scheduleGrav: Long = 0
    var life: Long = 8000

    /**
      * Render everything, run game logic.
      * @param delta    Time from last frame
      */
    override def render(delta: Float): Unit = {
        Gdx.gl glClearColor(0.133f, 0.133f, 0.133f, 1)
        Gdx.gl glClear GL20.GL_COLOR_BUFFER_BIT

        // Start batch
        game.batch setProjectionMatrix camera.combined
        game.batch begin()

        // Render field, score text
        for(r <- field.indices; c <- field(r).indices)
            game.batch draw(minoTypes(field(r)(c)), 16*c, 16*r)
        game.font draw(game.batch, "Score: " + score, 176, 304)

        // Render preview
        for(t <- preview.indices; r <- preview(t).matrix.indices; c <- preview(t).matrix(r).indices)
            if(preview(t).matrix(r)(c) == 1)
                game.batch draw(minoTypes(preview(t).minoType), 176 + 16*c, 16 + 64*(2 - t) + 16*r)

        // Render ghost piece
        var ghostY = player.y
        while(!collision(field, player.matrix, player.x, ghostY - 1))
            ghostY -= 1
        for(r <- player.matrix.indices; c <- player.matrix(r).indices; if player.matrix(r)(c) == 1)
            game.batch draw(ghostTypes(player.minoType), 16 * (c + player.x), 16 * (r + ghostY))

        // Render player
        for(r <- player.matrix.indices; c <- player.matrix(r).indices; if player.matrix(r)(c) == 1)
            game.batch draw(minoTypes(player.minoType), 16 * (c + player.x), 16 * (r + player.y))

        if(player.minoType == 0)
            game.font draw(game.batch, "Game over!\nPress R to replay", 176, 288)

        game.batch end()

        // Logic portion of render()

        // Helper method to end the game
        def gameOver() = player = new Tetromino(0)

        // Only allow inputs if we haven't stopped (player minoType = 0)
        if(player.minoType != 0) {
            // Tetromino rotation keys
            // TODO allow key mapping, that would be neato :ok_hand:
            def rotateHelper(r: Int = 0): Unit = {
                // I'm so sorry Scala gods
                for (
                    x <- List(0, -1, 1); y <- List(0, -1, 1)
                    if !collision(field, Tetromino.rotate(player.matrix, r), player.x + x, player.y + y)) {
                    player.matrix = Tetromino.rotate(player.matrix, r)
                    player.x += x
                    player.y += y
                    return
                }
            }

            // Rotate 270, 180, 90
            keyTap(Keys.Z, () => rotateHelper(2))
            keyTap(Keys.X, () => rotateHelper(1))
            keyTap(Keys.C, () => rotateHelper())
            // Leftwards movement
            scheduleLeft = keyHoldRepeat(Keys.LEFT, INITIAL_DELAY, REPEAT_DELAY, scheduleLeft, () => {
                if (!collision(field, player.matrix, player.x - 1, player.y))
                    player.x -= 1
            })
            // Rightwards movement
            scheduleRight = keyHoldRepeat(Keys.RIGHT, INITIAL_DELAY, REPEAT_DELAY, scheduleRight, () => {
                if (!collision(field, player.matrix, player.x + 1, player.y))
                    player.x += 1
            })
            // Downwards movement (Soft-drop)
            scheduleDown = keyHoldRepeat(Keys.DOWN, INITIAL_DELAY, REPEAT_DELAY, scheduleDown, () => {
                if (!collision(field, player.matrix, player.x, player.y - 1))
                    player.y -= 1
            })
            // Hard-drop (just zeroes player life)
            keyTap(Keys.SPACE, () => life = 0)

            // Gravity
            if(TimeUtils.millis() > scheduleGrav) {
                if(!collision(field, player.matrix, player.x, player.y - 1)) {
                    player.y -= 1
                    scheduleGrav = TimeUtils.millis() + gravityTime(GRAVITY, GRAVITY_ACCEL, score)
                }
                // If we can't move the player down, subtract from its life
                else {
                    life -= (1000 * delta).toLong
                }
            }

            // When the player runs out of life, bring the player to the bottom and add it to the field
            if(life <= 0) {
                // Perform a hard drop
                while(!collision(field, player.matrix, player.x, player.y - 1))
                    player.y -= 1

                // And add to the field, if a piece goes over the limit it's game over
                for (r <- player.matrix.indices; c <- player.matrix(r).indices; if player.matrix(r)(c) == 1) {
                    field(r + player.y)(c + player.x) = player.minoType
                }
                life = lifeTime(LIFE, LIFE_ACCEL, score)

                // Run line clearing
                var shift = 0
                for (r <- field.indices; if r + shift < field.length) {
                    while (!(field(r + shift) contains 0))
                        shift += 1
                    field(r) = field(r + shift).clone()
                }
                score += shift

                // Roll the recent, limit recent to a length of 8
                recent.enqueue(player.minoType)
                if (recent.length > 8)
                    recent.dequeue()

                // Roll the preview
                player = preview dequeue()
                preview += randomTetromino(true)

                // If the piece collides with the field on entry, then we can't continue: game over
                if(collision(field, player.matrix, player.x, player.y)) gameOver()
            }
        }
        else {
            keyTap(Keys.R, () => game setScreen(new TetrisScreen(game)))
        }
    }

    /**
      * Checks if the roaming matrix m collides with the fixed matrix (field) f.
      * M and f are 2-dimensional Arrays filled with Ints where 0 represents an empty space.
      * @param f    Fixed matrix
      * @param m    Roaming matrix
      * @param x    X-offset of m
      * @param y    Y-offset of m
      * @return True if a collision or out-of-bounds occurs, false otherwise
      */
    def collision(f: Array[Array[Int]], m: Array[Array[Int]], x: Int, y: Int): Boolean = {
        try {
            for (r <- m.indices; c <- m(r).indices; if m(r)(c) != 0) if (f(r + y)(c + x) != 0) return true
        }
        catch {
            case e: ArrayIndexOutOfBoundsException => return true
        }
        false
    }

    /**
      * Returns the next piece's gravity.
      * @param g    Initial gravity
      * @param a    Gravity acceleration
      * @param s    Score (lines)
      * @return Delta time for gravity
      */
    def gravityTime(g: Long, a: Double, s: Int): Long = (g * math.pow(a, s.toDouble / 50)).toLong

    /**
      * Returns the next piece's life.
      * @param l    Initial life
      * @param a    Life acceleration
      * @param s    Score (lines)
      * @return Delta time for life
      */
    def lifeTime(l: Long, a: Double, s: Int): Long = (250 + l * math.pow(a, s.toDouble / 50)).toLong

    /**
      * Returns a new random Tetromino.
      * The weighted distribution is a function that attempts to smooth the chances of the next Tetromino over the
      * the previous 8 Tetromino.
      * @param weightedDist Whether to use a weighted random function or not
      * @return New random Tetromino
      */
    def randomTetromino(weightedDist: Boolean = false): Tetromino = {
        if(weightedDist) {
            // Calculate the distribution
            val proportions = for(t <- 1 to 7) yield recent.count(_ == t) / recent.length.toDouble
            val weighted = for(t <- 0 to 6) yield 1 * (1 - proportions(t))
            val dist = weighted.scanLeft(0d)(_ + _)

            // Assign a new random Tetromino based on the distribution
            val p = random.nextDouble() * weighted.sum
            for(t <- dist.indices)
                if(p < dist(t))
                    return new Tetromino(t)
            randomTetromino()
        }
        else
            new Tetromino((7 * random.nextDouble()).toInt + 1)
    }

    /**
      * Reacts with a callback on key press.
      * Convenience method to reduce boilerplate.
      * @param key      Key to listen for
      * @param callback Function to call when the key is pressed
      */
    def keyTap(key: Int, callback: () => Unit): Unit = if(Gdx.input.isKeyJustPressed(key)) callback()

    /**
      * Repeats a callback after an initial delay, similar to Windows key repeat.
      * Convenience method to reduce boilerplate.
      * @param key          Key to listen for
      * @param initialDelay Initial key press delay
      * @param repeatDelay  Delay for each repetition
      * @param schedule     Next possible repeat time
      * @param callback     Function to call on repeat
      * @return New schedule time, make sure to set schedule to the return value of this function
      */
    def keyHoldRepeat(key: Int, initialDelay: Long, repeatDelay: Long, schedule: Long, callback: () => Unit): Long = {
        if(Gdx.input.isKeyJustPressed(key)) {
            callback()
            TimeUtils.millis() + initialDelay
        }
        else if(Gdx.input.isKeyPressed(key) && TimeUtils.millis() > schedule) {
            callback()
            TimeUtils.millis() + repeatDelay
        }
        else
            schedule
    }

    /**
      * Updates viewport.
      * @param width    Width of window
      * @param height   Height of window
      */
    override def resize(width: Int, height: Int): Unit = viewport update(width, height)
}
