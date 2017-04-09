package io.github.neilljohnston

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.{Gdx, ScreenAdapter}

class WelcomeScreen(val game: TetrisScala) extends ScreenAdapter {
    override def render(delta: Float): Unit = {
        Gdx.gl glClearColor(0.133f, 0.133f, 0.133f, 1)
        Gdx.gl glClear GL20.GL_COLOR_BUFFER_BIT

        game.batch.begin()
        game.font.draw(game.batch, "Welcome to Tetris\nPress R to begin", 16, 304)
        game.batch.end()

        keyTap(Keys.R, () => game.setScreen(new TetrisScreen(game)))
    }

    /**
      * Reacts with a callback on key press.
      * Convenience method to reduce boilerplate.
      * @param key      Key to listen for
      * @param callback Function to call when the key is pressed
      */
    def keyTap(key: Int, callback: () => Unit): Unit = if(Gdx.input.isKeyJustPressed(key)) callback()
}
