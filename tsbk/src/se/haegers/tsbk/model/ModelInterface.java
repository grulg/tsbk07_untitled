package se.haegers.tsbk.model;

import com.badlogic.gdx.graphics.Camera;

public interface ModelInterface {
	
	public void create();
	
	public void render(Camera camera);
	
	public void dispose();	
	
}
