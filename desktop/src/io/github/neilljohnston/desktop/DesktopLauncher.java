package io.github.neilljohnston.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.neilljohnston.TetrisScala;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Tetris Scala";
		config.width = 320;
		config.height = 320;
		new LwjglApplication(new TetrisScala(), config);
	}
}
