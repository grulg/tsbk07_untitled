package se.haegers.tsbk.ehager;

public class TerrainPoint
{
	public TerrainPoint(float x, float y, float z, int property) 
	{
		coords = new float[3];
		
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		this.property = property;
	}
	
	/**
	 * Creates a new terrain point identical to this one, but offset in space.
	 * @param xo
	 * @param yo
	 * @param zo
	 * @return
	 */
	public TerrainPoint getWithOffset(float xo, float yo, float zo)
	{
		return new TerrainPoint(coords[0]+xo, coords[1]+yo, coords[2]+zo, property);
	}
	
	public void setProperty(int property)
	{
		this.property = property;
	}
	
	public void setNormal(float[] n)
	{
		normal = n;
	}
	
	public float[] getNormal()
	{
		return normal;
	}
	
	public int getProperty()
	{
		return property;
	}
	
	public float getX()
	{
		return coords[0];
	}
	
	public float getY()
	{
		return coords[1];
	}
	
	public float getZ()
	{
		return coords[2];
	}
	
	public float[] getCoordsArr()
	{
		return coords;
	}
	
	private float[] coords;
	private float[] normal;
	
	private int property;
}
