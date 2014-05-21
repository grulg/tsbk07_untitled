package se.haegers.tsbk.ehager;

public class EdgeVertex 
{
	public EdgeVertex(float x, float y, float z)
	{
		coords = new float[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		
		normal = new float[3];
		
		normal[0] = 0.0f;
		normal[1] = 1.0f;
		normal[2] = 0.0f;
	}
	
	public EdgeVertex(TerrainPoint a, TerrainPoint b)
	{
		coords = new float[3];
		coords[0] = (a.getX()+b.getX())/2.0f;
		coords[1] = (a.getY()+b.getY())/2.0f;
		coords[2] = (a.getZ()+b.getZ())/2.0f;

		float[] an, bn;
		an = a.getNormal();
		bn = b.getNormal();
		
		normal = new float[3];
		normal[0] = (an[0]+bn[0]);
		normal[1] = (an[1]+bn[1]);
		normal[2] = (an[2]+bn[2]);
		
		float l = (float) Math.sqrt(
				normal[0]*normal[0]+
				normal[1]*normal[1]+
				normal[2]*normal[2]);
		
		if(l > 0)
		{
			normal[0] /= l;
			normal[1] /= l;
			normal[2] /= l;
		}
		else
		{
			normal[0] = 0.0f;
			normal[1] = 1.0f;
			normal[2] = 0.0f;
		}
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
	
	public float[] getNormArr()
	{
		return normal;
	}
	
	public float[] getCoordsArr()
	{
		return coords;
	}
	
	private float[] coords;
	private float[] normal;
}
