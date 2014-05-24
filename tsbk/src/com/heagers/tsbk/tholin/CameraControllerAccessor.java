package com.heagers.tsbk.tholin;

import aurelienribon.tweenengine.TweenAccessor;

public class CameraControllerAccessor implements TweenAccessor<CameraController> {
	
	// The following are the possible tween types for the camera
	public static final int POSITION_X = 1;
	public static final int POSITION_Y = 2;
	public static final int POSITION_Z = 3;
	public static final int POSITION_XY = 4;
	public static final int POSITION_XZ = 5;
	public static final int POSITION_YZ = 6;
	public static final int POSITION_XYZ = 7;
	
	// The actual implementation

	@Override
	public int getValues(CameraController target, int tweenType, float[] returnValues) {
		switch(tweenType) {
		case POSITION_X: returnValues[0] = target.getCamera().position.x; return 1;
		case POSITION_Y: returnValues[0] = target.getCamera().position.y; return 1;
		case POSITION_Z: returnValues[0] = target.getCamera().position.z; return 1;
		case POSITION_XY:
			returnValues[0] = target.getCamera().position.x;
			returnValues[1] = target.getCamera().position.y;
			return 2;
		case POSITION_XZ:
			returnValues[0] = target.getCamera().position.x;
			returnValues[1] = target.getCamera().position.z;
			return 2;
		case POSITION_YZ:
			returnValues[0] = target.getCamera().position.y;
			returnValues[1] = target.getCamera().position.z;
			return 2;
		case POSITION_XYZ:
			returnValues[0] = target.getCamera().position.x;
			returnValues[1] = target.getCamera().position.y;
			returnValues[1] = target.getCamera().position.z;
			return 3;
		default: assert false; return -1;
		}
	}

	@Override
	public void setValues(CameraController target, int tweenType, float[] newValues) {
		switch(tweenType){
		case POSITION_X: target.getCamera().position.x = newValues[0]; break;
		case POSITION_Y: target.getCamera().position.y = newValues[0]; break;
		case POSITION_Z: target.getCamera().position.z = newValues[0]; break;
		case POSITION_XY:
			target.getCamera().position.x = newValues[0];
			target.getCamera().position.y = newValues[1]; 
			break;
		case POSITION_XZ:
			target.getCamera().position.x = newValues[0];
			target.getCamera().position.z = newValues[1]; 
			break;
		case POSITION_YZ:
			target.getCamera().position.y = newValues[0];
			target.getCamera().position.z = newValues[1]; 
			break;
		case POSITION_XYZ:
			target.getCamera().position.x = newValues[0];
			target.getCamera().position.y = newValues[1];
			target.getCamera().position.z = newValues[2];
			break;
		 default: assert false; break;
		}
	}

}
