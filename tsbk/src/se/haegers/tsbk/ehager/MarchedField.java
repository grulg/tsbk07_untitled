package se.haegers.tsbk.ehager;

import java.util.Vector;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MarchedField 
{

	public MarchedField(int len)
	{
		dimX = len;
		dimZ = len;
		dimY = len;
		spacing = 1.0f;
		activeVerts = new Vector<TerrainPoint>();
		
		field = new TerrainPoint[len][len][len];
		int type = 0;
		
		for(int x=0; x < dimX; ++x)
		{
			for(int y=0; y < dimY; ++y)
			{
				for(int z=0; z < dimZ; ++z)
				{
					if(y < dimY/2 || ((x == dimX/2 || z == dimZ/2)&& y < dimY-2))
						type = 1;
					else if (y < 3*dimY/4)
						type = 2;
					else
						type = 0;
					
					field[x][y][z] = new TerrainPoint(x*spacing, y*spacing, z*spacing, type);
				}
			}
		}
		
	}
	
	public MarchedField(HeightMap source)
	{
		dimX = source.getW();
		dimZ = source.getH();
		dimY = 64;
		
		activeVerts = new Vector<TerrainPoint>();
		
		spacing = 1.0f;
		
		double frac;
		double waterHeight = 0.2;
		
		int type;
		
		field = new TerrainPoint[dimX][dimY][dimZ];
		
		for(int x=0; x < dimX; ++x)
		{
			for(int y=0; y < dimY; ++y)
			{
				for(int z=0; z < dimZ; ++z)
				{
					if(y==0)
						type = 1;
					else
					{
						type = 0;
						frac = (float)y/(dimY/2);
						
						if(frac < source.getHeight(x, z))
							type = 1;
						else if(frac < waterHeight)
							 type = 2;
					}
					
					field[x][y][z] = new TerrainPoint(x*spacing, y*spacing, z*spacing, type);
				}
			}
		}
		System.out.println("Made point-field\n");
	}
	
	public void renderNormals(ShapeRenderer s)
	{
		EdgeVertex t;
		for(int q=0; q < edges.size(); ++q)
		{
			t = edges.get(q);
			s.line(t.getX(), t.getY(), t.getZ(), 
					t.getX()+0.5f*t.getNormArr()[0], 
					t.getY()+0.5f*t.getNormArr()[1],
					t.getZ()+0.5f*t.getNormArr()[2]);
		}
	}
	
	public void renderPoints(ShapeRenderer s)
	{
		for(int x=0; x < dimX; ++x)
		{
			for(int y=0; y < dimY; ++y)
			{
				for(int z=0; z < dimZ; ++z)
				{
					TerrainPoint t = field[x][y][z];
					
					if(t.getProperty() == 1)
						s.setColor(Color.GREEN);
					else
						s.setColor(Color.BLUE);
					s.point(t.getX(), t.getY(), t.getZ());
				}
			}
		}
	}
	
	public float pointState(float x, float y, float z)
	{
		float ret = 0;
		
		float xD, yD, zD;
		xD = x/spacing;
		yD = y/spacing;
		zD = z/spacing;
		int sX, sY, sZ;
		sX = (int) Math.floor(xD);
		sY = (int) Math.floor(yD);
		sZ = (int) Math.floor(zD);
		float fracX = xD-sX;
		float fracY = yD-sY;
		float fracZ = zD-sZ;
		
		if(sX < 0 || sX >= dimX-1)
			return 0.0f;
		if(sY < 0 || sY >= dimY-1)
			return 0.0f;
		if(sZ < 0 || sZ >= dimZ-1)
			return 0.0f;
		
		float xyz = (float)field[sX][sY][sZ].getProperty();
		float x1yz = (float)field[sX+1][sY][sZ].getProperty();
		float xy1z = (float)field[sX][sY+1][sZ].getProperty();
		float xyz1 = (float)field[sX][sY][sZ+1].getProperty();
		float xy1z1 = (float)field[sX][sY+1][sZ+1].getProperty();
		float x1yz1 = (float)field[sX+1][sY][sZ+1].getProperty();
		float x1y1z = (float)field[sX+1][sY+1][sZ].getProperty();
		float x1y1z1 = (float)field[sX+1][sY+1][sZ+1].getProperty();
		
		float x1 = xyz + (x1yz-xyz)*fracX;
		float x2 = xy1z + (x1y1z-xy1z)*fracX;
		float x3 = xyz1 + (x1yz1-xyz1)*fracX;
		float x4 = xy1z1 + (x1y1z1-xy1z1)*fracX;
		
		float y1 = x1+(x2-x1)*fracY;	
		float y2 = x3+(x4-x3)*fracY;
		
		ret = y1+(y2-y1)*fracZ;
		
		return ret;
	}
	
	public void activatePointsInRadius(float x, float y, float z, float r)
	{
		float xD, yD, zD;
		xD = x/spacing;
		yD = y/spacing;
		zD = z/spacing;
		int n = (int) Math.floor(2*r+0.5);
		
		int iX, iY, iZ, sX, sY, sZ;
		sX = (int) Math.floor(xD+0.5f);
		sY = (int) Math.floor(yD+0.5f);
		sZ = (int) Math.floor(zD+0.5f);

		sX -= n/2;
		sY -= n/2;
		sZ -= n/2;
		float rs = r*r;
		
		activeVerts = new Vector<TerrainPoint>();
		
		iX = sX;
		float curX, curY, curZ;
		float xDi, yDi, zDi;
		for(int q=0; q < n; ++q)
		{
			if(iX >= dimX || iX < 0)
			{ continue; }
			iY = sY;
			for(int w=0; w < n; ++w)
			{
				if(iY >= dimY || iY < 0)
				{ continue; }
				iZ = sZ;
				for(int e=0; e < n; ++e)
				{
					if(iZ >= dimZ || iZ < 0)
					{ continue; }
					
					curX = iX*spacing;
					curY = iY*spacing;
					curZ = iZ*spacing;
					
					xDi = curX-xD;
					yDi = curY-yD;
					zDi = curZ-zD;
					
					if((xDi*xDi+yDi*yDi+zDi*zDi) <= rs)
						activeVerts.add(field[iX][iY][iZ]);
					++iZ;
				}
				++iY;
			}
			++iX;
		}
	}
	
	public void activatePointsCloseTo(float x, float y, float z, int n)
	{
		float xD, yD, zD;
		xD = x/spacing;
		yD = y/spacing;
		zD = z/spacing;
		
		int iX, iY, iZ, sX, sY, sZ;
		sX = (int) Math.floor(xD+0.5f);
		sY = (int) Math.floor(yD+0.5f);
		sZ = (int) Math.floor(zD+0.5f);
		
		sX -= n/2;
		sY -= n/2;
		sZ -= n/2;
		
		activeVerts = new Vector<TerrainPoint>();
		
		iX = sX;
		for(int q=0; q < n; ++q)
		{
			if(iX >= dimX || iX < 0)
			{ continue; }
			iY = sY;
			for(int w=0; w < n; ++w)
			{
				if(iY >= dimY || iY < 0)
				{ continue; }
				iZ = sZ;
				for(int e=0; e < n; ++e)
				{
					if(iZ >= dimZ || iZ < 0)
					{ continue; }
					
					activeVerts.add(field[iX][iY][iZ]);
					++iZ;
				}
				++iY;
			}
			++iX;
		}
	}	
	
	public TerrainPoint[] getActivePoints()
	{
		TerrainPoint[] ret = new TerrainPoint[activeVerts.size()];
		for(int q=0; q < activeVerts.size(); ++q)
			ret[q] = activeVerts.get(q);
		return ret;
	}
	
	public int countActivePoints()
	{
		return activeVerts.size();
	}
	
	public void setActivePointsProperty(int p)
	{
		for(int q=0; q < activeVerts.size(); ++q)
			activeVerts.get(q).setProperty(p);
	}
	
	public void setFieldProperty(int x, int y, int z, int p)
	{
		if(field[x][y][z].getProperty() != p)
		{
			field[x][y][z].setProperty(p);
			//dafuq is this doing here, take it away plx
			refreshSolidNormals(x-1, y-1, z-1, x+1, y+1, z+1);
			makeSolidEdges(1,2,1,2,1,2);
		}
	}
	
	public Mesh getCurrentMesh()
	{
		if(edges.size() == 0)
			return null;
		
		Mesh ret = new Mesh(true, edges.size(), 0,
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Normal, 3, "a_normal"));
		
		float[] pos = new float[edges.size()*6];
		
		for(int q=0; q < edges.size(); ++q)
		{
			pos[ 6*q ] = edges.get(q).getX();
			pos[6*q+1] = edges.get(q).getY();
			pos[6*q+2] = edges.get(q).getZ();
			
			pos[6*q+3] = edges.get(q).getNormArr()[0];
			pos[6*q+4] = edges.get(q).getNormArr()[1];
			pos[6*q+5] = edges.get(q).getNormArr()[2];
		}
		
		ret.setVertices(pos);
		
		return ret;
	}
	
	public void refreshWaterNormals(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		if(x1 < 0) x1 = 0;	if(x2 >= dimX) x2 = dimX-1;
		if(y1 < 0) y1 = 0;	if(y2 >= dimY) y2 = dimY-1;
		if(z1 < 0) z1 = 0;	if(z2 >= dimZ) z2 = dimZ-1;
		for(int x=x1; x <= x2; ++x)
		{
			for(int y=y1; y <= y2; ++y)
			{
				for(int z=z1; z <= z2; ++z)
				{
					field[x][y][z].setNormal(getWaterNormal(x,y,z));
				}
			}
		}
	}
	
	public void refreshSolidNormals(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		if(x1 < 0) x1 = 0;	if(x2 >= dimX) x2 = dimX-1;
		if(y1 < 0) y1 = 0;	if(y2 >= dimY) y2 = dimY-1;
		if(z1 < 0) z1 = 0;	if(z2 >= dimZ) z2 = dimZ-1;
		for(int x=x1; x <= x2; ++x)
		{
			for(int y=y1; y <= y2; ++y)
			{
				for(int z=z1; z <= z2; ++z)
				{
					field[x][y][z].setNormal(getSolidNormal(x,y,z));
				}
			}
		}
	}
	
	public void setSolidNormals()
	{
		for(int x=0; x < dimX; ++x)
		{
			for(int y=0; y < dimY; ++y)
			{
				for(int z=0; z < dimZ; ++z)
				{					
					field[x][y][z].setNormal(getSolidNormal(x, y, z));
				}
			}
		}
	}
	
	public float getSpacing()
	{
		return spacing;
	}
	
	private float[] getWaterNormal(int x, int y, int z)
	{
		//TODO Proper normals for water, but it's almost always going to be flat anyway, so who cares?
		return new float[] {0.0f, 1.0f, 0.0f};
	}
	
	private float[] getSolidNormal(int x, int y, int z)
	{
		float[] ret = new float[3];
		int xL, xH;
		xL = getFieldProperty(x-1, y, z);
		xH = getFieldProperty(x+1, y, z);
		
		float xLF, xHF;
		if(xL != field[x][y][z].getProperty())
			xLF = 0.5f;
		else if(xL == 1)
			xLF = 1.0f;
		else
			xLF = 0.0f;
		if(xH != field[x][y][z].getProperty())
			xHF = 0.5f;
		else if(xH == 1)
			xHF = 1.0f;
		else
			xHF = 0.0f;
		
		int yL, yH;
		yL = getFieldProperty(x, y-1, z);
		yH = getFieldProperty(x, y+1, z);
		
		float yLF, yHF;
		if(yL != field[x][y][z].getProperty())
			yLF = 0.5f;
		else if(yL == 1)
			yLF = 1.0f;
		else
			yLF = 0.0f;
		
		if(yH != field[x][y][z].getProperty())
			yHF = 0.5f;
		else if(yH == 1)
			yHF = 1.0f;
		else
			yHF = 0.0f;
	
		int zL, zH;
		zL = getFieldProperty(x, y, z-1);
		zH = getFieldProperty(x, y, z+1);
		
		float zLF, zHF;
		if(zL != field[x][y][z].getProperty())
			zLF = 0.5f;
		else if(zL == 1)
			zLF = 1.0f;
		else
			zLF = 0.0f;
		
		if(zH != field[x][y][z].getProperty())
			zHF = 0.5f;
		else if(zH == 1)
			zHF = 1.0f;
		else
			zHF = 0.0f;
		
		ret[0] = xLF - xHF;
		ret[1] = yLF - yHF;
		ret[2] = zLF - zHF;
		
		return ret;
	}
	
	private int getFieldProperty(int x, int y, int z)
	{
		if(x < 0)
			x = 0;
		else if(x >= dimX)
			x = dimX-1;
		if(y < 0)
			y = 0;
		else if(y >= dimY)
			y = dimY-1;
		if(z < 0)
			z = 0;
		else if(z >= dimZ)
			z = dimZ-1;
		
		return field[x][y][z].getProperty();
	}
	
	public int getDimX()
	{
		return dimX;
	}
	public int getDimY()
	{
		return dimY;
	}
	public int getDimZ()
	{
		return dimZ;
	}
	
	public void makeWaterEdges(int xs, int xe, int ys, int ye, int zs, int ze)
	{
		if(xs < 0)		xs = 0;
		if(xe <= xs)	xe = xs+1;
		if(xe >= dimX-1)xe = dimX-2;		
		if(ys < 0)		ys = 0;
		if(ye <= ys)	ye = ys+1;
		if(ye >= dimY-1)ye = dimY-2;
		if(zs < 0)		zs = 0;
		if(ze <= zs)	ze = zs+1;
		if(ze >= dimZ-1)ze = dimZ-2;
		
		edges = new Vector<EdgeVertex>();
		for(int x=xs; x < xe; ++x)
		{
			for(int y=ys; y < ye; ++y)
			{
				for(int z=zs; z < ze; ++z)
				{
					int ind = 0;
					
					if(field[ x ][ y ][ z ].getProperty() == 2) ind |= 1;
					if(field[x+1][ y ][ z ].getProperty() == 2) ind |= 2;
					if(field[x+1][ y ][z+1].getProperty() == 2) ind |= 4;
					if(field[ x ][ y ][z+1].getProperty() == 2) ind |= 8;
					if(field[ x ][y+1][ z ].getProperty() == 2) ind |= 16;
					if(field[x+1][y+1][ z ].getProperty() == 2) ind |= 32;
					if(field[x+1][y+1][z+1].getProperty() == 2)	ind |= 64;
					if(field[ x ][y+1][z+1].getProperty() == 2) ind |= 128;
					
					/*
					if(ind != 0)
					{
						if(field[ x ][ y ][ z ].getProperty() == 1) ind |= 1;
						if(field[x+1][ y ][ z ].getProperty() == 1) ind |= 2;
						if(field[x+1][ y ][z+1].getProperty() == 1) ind |= 4;
						if(field[ x ][ y ][z+1].getProperty() == 1) ind |= 8;
						//if(ind >= 16)
						{
						if(field[ x ][y+1][ z ].getProperty() == 1) ind |= 16;
						if(field[x+1][y+1][ z ].getProperty() == 1) ind |= 32;
						if(field[x+1][y+1][z+1].getProperty() == 1)	ind |= 64;
						if(field[ x ][y+1][z+1].getProperty() == 1) ind |= 128;
						}
					}
					*/
					
					edgesAtIndex(x,y,z,ind);
				}
			}
		}
	}
	
	public void makeSolidEdges(int xs, int xe, int ys, int ye, int zs, int ze)
	{
		//Boundary checks: for easy thinking later.
		//No need for special cases when making terrain chunks.
		//System.out.printf("Solid edges: %d - %d,  %d - %d, %d - %d\n", xs, xe, ys, ye, zs, ze);
		if(xs < 0)		xs = 0;
		if(xe <= xs)	xe = xs+1;
		if(xe >= dimX-1)xe = dimX-2;		
		if(ys < 0)		ys = 0;
		if(ye <= ys)	ye = ys+1;
		if(ye >= dimY-1)ye = dimY-2;
		if(zs < 0)		zs = 0;
		if(ze <= zs)	ze = zs+1;
		if(ze >= dimZ-1)ze = dimZ-2;
		
		edges = new Vector<EdgeVertex>();
		for(int x=xs; x < xe; ++x)
		{
			for(int y=ys; y < ye; ++y)
			{
				for(int z=zs; z < ze; ++z)
				{
					int ind = 0;
					if(field[ x ][ y ][ z ].getProperty() == 1) ind |= 1;
					if(field[x+1][ y ][ z ].getProperty() == 1) ind |= 2;
					if(field[x+1][ y ][z+1].getProperty() == 1) ind |= 4;
					if(field[ x ][ y ][z+1].getProperty() == 1) ind |= 8;
					if(field[ x ][y+1][ z ].getProperty() == 1) ind |= 16;
					if(field[x+1][y+1][ z ].getProperty() == 1) ind |= 32;
					if(field[x+1][y+1][z+1].getProperty() == 1)	ind |= 64;
					if(field[ x ][y+1][z+1].getProperty() == 1) ind |= 128;
					
					edgesAtIndex(x, y, z, ind);
				}
			}
		}
	}
	
	private void edgesAtIndex(int x, int y, int z, int ind)
	{		
		
		int val = edgeTable[ind];
		
		if(val == 0)
			return;
		
		EdgeVertex[] potential = new EdgeVertex[15];
		if( (val & 1) != 0)
			potential[0] = new EdgeVertex(field[x][y][z], field[x+1][y][z]);
		if( (val & 2) != 0)
			potential[1] = new EdgeVertex(field[x+1][y][z], field[x+1][y][z+1]);
		if( (val & 4) != 0)
			potential[2] = new EdgeVertex(field[x+1][y][z+1], field[x][y][z+1]);
		if( (val & 8) != 0)
			potential[3] = new EdgeVertex(field[x][y][z+1], field[x][y][z]);
		if( (val & 16) != 0)	
			potential[4] = new EdgeVertex(field[x][y+1][z], field[x+1][y+1][z]);
		if( (val & 32) != 0)
			potential[5] = new EdgeVertex(field[x+1][y+1][z], field[x+1][y+1][z+1]);
		if( (val & 64) != 0)
			potential[6] = new EdgeVertex(field[x+1][y+1][z+1], field[x][y+1][z+1]);
		if( (val & 128) != 0)
			potential[7] = new EdgeVertex(field[x][y+1][z+1], field[x][y+1][z]);
		if( (val & 256) != 0)
			potential[8] = new EdgeVertex(field[x][y][z], field[x][y+1][z]);
		if( (val & 512) != 0)
			potential[9] = new EdgeVertex(field[x+1][y][z], field[x+1][y+1][z]);
		if( (val & 1024) != 0)
			potential[10] = new EdgeVertex(field[x+1][y][z+1], field[x+1][y+1][z+1]);
		if( (val & 2048) != 0)
			potential[11] = new EdgeVertex(field[x][y][z+1], field[x][y+1][z+1]);
		
		int[] tris = triTable[ind];
		for(int q=0; q < 15; ++q)
		{
			if(tris[q] == -1)
				break;
			edges.add(potential[tris[q]]);
		}
	}
	
	private TerrainPoint[][][] field;
	private Vector<TerrainPoint> activeVerts;
	private int dimX, dimY, dimZ;
	private float spacing;
	private Vector<EdgeVertex> edges;
	
	private static final int edgeTable[] = new int[] 
			{0x0, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c,
			0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00,
			0x190, 0x99 , 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c,
			0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90,
			0x230, 0x339, 0x33 , 0x13a, 0x636, 0x73f, 0x435, 0x53c,
			0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30,
			0x3a0, 0x2a9, 0x1a3, 0xaa , 0x7a6, 0x6af, 0x5a5, 0x4ac,
			0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0,
			0x460, 0x569, 0x663, 0x76a, 0x66 , 0x16f, 0x265, 0x36c,
			0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60,
			0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff , 0x3f5, 0x2fc,
			0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0,
			0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55 , 0x15c,
			0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950,
			0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc ,
			0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0,
			0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc,
			0xcc , 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0,
			0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c,
			0x15c, 0x55 , 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650,
			0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc,
			0x2fc, 0x3f5, 0xff , 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
			0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c,
			0x36c, 0x265, 0x16f, 0x66 , 0x76a, 0x663, 0x569, 0x460,
			0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac,
			0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa , 0x1a3, 0x2a9, 0x3a0,
			0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c,
			0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x33 , 0x339, 0x230,
			0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c,
			0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99 , 0x190,
			0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c,
			0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0};
	
	private static final int triTable[][] = new int[][]
		{{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1},
		{3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1},
		{3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1},
		{3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1},
		{9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1},
		{9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
		{2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1},
		{8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1},
		{9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
		{4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1},
		{3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1},
		{1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1},
		{4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1},
		{4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1},
		{9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
		{5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1},
		{2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1},
		{9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
		{0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
		{2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1},
		{10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1},
		{4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1},
		{5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1},
		{5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1},
		{9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1},
		{0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1},
		{1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1},
		{10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1},
		{8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1},
		{2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1},
		{7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1},
		{9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1},
		{2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1},
		{11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1},
		{9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1},
		{5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1},
		{11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1},
		{11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
		{1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1},
		{9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1},
		{5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1},
		{2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
		{0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
		{5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1},
		{6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1},
		{3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1},
		{6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1},
		{5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1},
		{1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
		{10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1},
		{6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1},
		{8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1},
		{7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1},
		{3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
		{5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1},
		{0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1},
		{9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1},
		{8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1},
		{5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1},
		{0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1},
		{6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1},
		{10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1},
		{10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1},
		{8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1},
		{1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1},
		{3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1},
		{0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1},
		{10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1},
		{3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1},
		{6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1},
		{9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1},
		{8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1},
		{3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1},
		{6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1},
		{0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1},
		{10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1},
		{10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1},
		{2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1},
		{7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1},
		{7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1},
		{2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1},
		{1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1},
		{11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1},
		{8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1},
		{0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1},
		{7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
		{10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
		{2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
		{6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1},
		{7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1},
		{2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1},
		{1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1},
		{10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1},
		{10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1},
		{0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1},
		{7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1},
		{6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1},
		{8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1},
		{9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1},
		{6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1},
		{4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1},
		{10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1},
		{8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1},
		{0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1},
		{1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1},
		{8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1},
		{10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1},
		{4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1},
		{10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
		{5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
		{11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1},
		{9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
		{6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1},
		{7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1},
		{3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1},
		{7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1},
		{9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1},
		{3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1},
		{6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1},
		{9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1},
		{1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1},
		{4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1},
		{7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1},
		{6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1},
		{3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1},
		{0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1},
		{6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1},
		{0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1},
		{11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1},
		{6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1},
		{5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1},
		{9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1},
		{1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1},
		{1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1},
		{10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1},
		{0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1},
		{5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1},
		{10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1},
		{11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1},
		{9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1},
		{7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1},
		{2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1},
		{8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1},
		{9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1},
		{9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1},
		{1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1},
		{9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1},
		{9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1},
		{5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1},
		{0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1},
		{10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1},
		{2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1},
		{0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1},
		{0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1},
		{9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1},
		{5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1},
		{3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1},
		{5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1},
		{8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1},
		{0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1},
		{9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1},
		{0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1},
		{1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1},
		{3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1},
		{4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1},
		{9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1},
		{11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1},
		{11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1},
		{2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1},
		{9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1},
		{3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1},
		{1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1},
		{4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1},
		{4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1},
		{0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1},
		{3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1},
		{3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1},
		{0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1},
		{9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1},
		{1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};
}
