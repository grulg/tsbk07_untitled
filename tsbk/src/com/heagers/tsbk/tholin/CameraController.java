package com.heagers.tsbk.tholin;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
/**
 * Class used for storing and handling the camera.
 * @author Emil
 *
 */

public class CameraController implements InputProcessor {
	private enum Move_Buttons {
		CAM_LEFT, 
		CAM_RIGHT, 
		CAM_UP, 
		CAM_DOWN
	}
	
	static Map<Move_Buttons, Boolean> buttons = new HashMap<Move_Buttons, Boolean>();
	static {
		buttons.put(Move_Buttons.CAM_LEFT, false);
		buttons.put(Move_Buttons.CAM_RIGHT, false);
		buttons.put(Move_Buttons.CAM_UP, false);
		buttons.put(Move_Buttons.CAM_DOWN, false);
	};
	
	private boolean leftPressed = false;
	private int screen_x = 0, screen_y = 0;
	
	private PerspectiveCamera camera;
	private static final float CAMERA_SCALE = 1f;
	private static final float ZOOM_SPEED = 5.0f * CAMERA_SCALE;
	private static final float CAMERA_MOVEMENT_SPEED = 0.5f * CAMERA_SCALE;
	private static final float CAMERA_ROTATE_SPEED = 2.0f;
	
	public CameraController() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera 		= new PerspectiveCamera(67, w, h);
		camera.far 	= 1000;
		camera.near = 0.1f;
		
		camera.translate(new Vector3(0,30,30));
		camera.rotate(-30, 1, 0, 0);
		camera.update();
	}
	
	public PerspectiveCamera getCamera() {
		return camera;
	}

	public void updateCamera() {
    	/*
    	 * When we want to move right we simply cross the up- and direction-vector of the frustum
    	 * and normalize it, and than scale it with the camera movement speed. Simple.
    	 */
    	if (buttons.get(Move_Buttons.CAM_LEFT)) {
    		camera.translate(camera.up.cpy().crs(camera.direction).nor().scl(CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		if (buttons.get(Move_Buttons.CAM_RIGHT)) {
			camera.translate(camera.up.cpy().crs(camera.direction).nor().scl(-CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		/*
    	 * When we want to move up we simply use the up vector. Sweet!
    	 */
		if (buttons.get(Move_Buttons.CAM_UP)) {
			camera.translate(camera.up.cpy().nor().scl(CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		if (buttons.get(Move_Buttons.CAM_DOWN)) {
			camera.translate(camera.up.cpy().nor().scl(-CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		if(leftPressed) {
			int x = Gdx.input.getX();
			int y = Gdx.input.getY();
			int deltaX = screen_x - x;
			int deltaY = screen_y - y;
			
			/*
			 * If the mouse has moved to the right, we simply rotate the camera to the right around the camera position
			 * with the up-vector as rotation axis.
			 */
			if(deltaX > 0) {
				camera.rotateAround(camera.position, camera.up, CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			else if(deltaX < 0) {
				camera.rotateAround(camera.position, camera.up, -CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			
			/*
			 * If the mouse moved up, we rotate the camera up by crossing the up vector with the direction vector to
			 * get the axis we want to rotate around (and finally normalize it), and we use camera position 
			 * as rotation point as before.
			 */
			if(deltaY > 0) {
				camera.rotateAround(camera.position, camera.up.cpy().crs(camera.position).nor(), CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			else if(deltaY < 0) {
				camera.rotateAround(camera.position, camera.up.cpy().crs(camera.position).nor(), -CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			
			// Set the current mouse position as previous mouse position, so that we can use it in the next iteration
			screen_x = x;
			screen_y = y;
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Keys.A) {
			buttons.get(buttons.put(Move_Buttons.CAM_LEFT, true));
		}
		if(keycode == Keys.D) {
			buttons.get(buttons.put(Move_Buttons.CAM_RIGHT, true));
		}
		if(keycode == Keys.W) {
			buttons.get(buttons.put(Move_Buttons.CAM_UP, true));
		}
		if(keycode == Keys.S) {
			buttons.get(buttons.put(Move_Buttons.CAM_DOWN, true));
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.A) {
			buttons.get(buttons.put(Move_Buttons.CAM_LEFT, false));
		}
		if(keycode == Keys.D) {
			buttons.get(buttons.put(Move_Buttons.CAM_RIGHT, false));
		}
		if(keycode == Keys.W) {
			buttons.get(buttons.put(Move_Buttons.CAM_UP, false));
		}
		if(keycode == Keys.S) {
			buttons.get(buttons.put(Move_Buttons.CAM_DOWN, false));
		}
		
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(button == Buttons.LEFT) {
			leftPressed = false;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(pointer == Buttons.LEFT) {
			leftPressed = true;
		}
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		camera.translate(camera.direction.cpy().nor().scl(-amount * ZOOM_SPEED));
		camera.update();
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
}
