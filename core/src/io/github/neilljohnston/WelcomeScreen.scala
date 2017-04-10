package io.github.neilljohnston

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.{Color, GL20, Texture}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.{Image, Label}
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.{Gdx, ScreenAdapter}

class WelcomeScreen(val game: TetrisScala) extends ScreenAdapter {
    val viewport = new FitViewport(368, 368)
    val stage = new Stage(viewport)

    // Border
    val borderPatch = new NinePatch(new Texture(Gdx.files.internal("border-default.9.png")), 16, 16, 16, 16)
    val border = new Image(borderPatch)
    border setSize(368, 368)
    border setPosition(0, 0)
    stage addActor border

    // Welcome label
    val welcomeStyle = new LabelStyle(game.font, Color.WHITE)
    val welcome = new Label("Welcome to Tetris\nPress R to begin", welcomeStyle)
    welcome setSize(368, 368)
    welcome setPosition(0, 0)
    welcome setAlignment Align.center
    stage addActor welcome

    override def render(delta: Float): Unit = {
        Gdx.gl glClearColor(0.133f, 0.133f, 0.133f, 1)
        Gdx.gl glClear GL20.GL_COLOR_BUFFER_BIT

        game.batch begin()
        stage act()
        stage draw()
        game.batch end()

        keyTap(Keys.R, () => game.setScreen(new TetrisScreen(game)))
    }

    /**
      * Reacts with a callback on key press.
      * Convenience method to reduce boilerplate.
      * @param key      Key to listen for
      * @param callback Function to call when the key is pressed
      */
    def keyTap(key: Int, callback: () => Unit): Unit = if(Gdx.input.isKeyJustPressed(key)) callback()

    /**
      * Updates viewport.
      * @param width    Width of window
      * @param height   Height of window
      */
    override def resize(width: Int, height: Int): Unit = viewport update(width, height)
}
