package se.haegers.tsbk.ehager;

import com.badlogic.gdx.math.Vector3;

public abstract class BoxShaped 
{
	public BoxShaped(float xs, float ys, float zs)
	{
		warpTo(0.0f, 0.0f, 0.0f);
		dimX = xs/2.0f;
		dimY = ys/2.0f;
		dimZ = zs/2.0f;
		
	}
	public BoxShaped(Vector3 dims)
	{
		warpTo(0.0f, 0.0f, 0.0f);
		dimX = dims.x;
		dimY = dims.y;
		dimZ = dims.z;
	}
	public BoxShaped(float x, float y, float z, float xs, float ys, float zs)
	{
		warpTo(x,y,z);
		dimX = xs/2.0f;
		dimY = ys/2.0f;
		dimZ = zs/2.0f;
	}
	public BoxShaped(Vector3 pos, Vector3 dims)
	{
		warpTo(pos);
		dimX = dims.x;
		dimY = dims.y;
		dimZ = dims.z;
	}
	
	public void warpTo(float x, float y, float z)
	{
		centerX = x;
		centerY = y;
		centerZ = z;
	}
	
	public void warpTo(Vector3 pos)
	{
		centerX = pos.x;
		centerY = pos.y;
		centerZ = pos.z;
	}
	public boolean overlap(BoxShaped s)
	{
		boolean ret = false;
		//This three-if stair is a super good-looking piece of code.
		//Yeah, screw those curly brackets. Who needs clarity?
		if(Math.abs(centerX-s.centerX) <= dimX+s.dimX)
			if(Math.abs(centerZ-s.centerZ) <= dimZ+s.dimZ)
				if(Math.abs(centerY-s.centerY) <= dimY+s.dimY)
					ret = true;
		return ret;
	}
	
	public boolean isInside(float x, float y, float z)
	{
		boolean ret = false;
		if(Math.abs(centerZ-z) <= dimZ)
			if(Math.abs(centerX-x) <= dimX)
				if(Math.abs(centerY-y) <= dimY)
					ret = true;
		return ret;
	}
	public boolean isInside(Vector3 pos)
	{
		return isInside(pos.x, pos.y, pos.z);
	}
	
	//Origin is in the middle, not the upper left back corner or whatever.
	private float centerX, centerY, centerZ;
	//For most calculations, we'll be using sidelength/2, so store that instead.
	private float dimX, dimY, dimZ;
}
