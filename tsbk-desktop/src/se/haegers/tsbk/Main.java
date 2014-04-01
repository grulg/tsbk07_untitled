package se.haegers.tsbk;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "tsbk";
		cfg.useGL30 = false;
		cfg.width = 800;//480;
		cfg.height = 600;//320;
		
		new LwjglApplication(new TSBK(), cfg);
	}
}
