package io.github.neilljohnston

import java.io.File

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}

class TetrisScala extends Game {
    var batch: SpriteBatch = _
    var font: BitmapFont = _

    /**
      * Creates the game, sets screen to Tetris.
      */
    override def create(): Unit = {
        batch = new SpriteBatch()
        font = new BitmapFont()

        setScreen(new WelcomeScreen(this))
    }

    override def render(): Unit = super.render()

    override def dispose(): Unit = {
        batch dispose()
        font dispose()
    }
}
