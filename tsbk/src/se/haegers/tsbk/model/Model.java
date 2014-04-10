package se.haegers.tsbk.model;

import com.badlogic.gdx.math.Matrix4;

public interface Model {
	
	public void load();
	
	public void render(Matrix4 cameraMatrix);
	
	public void dispose();	
	
}
