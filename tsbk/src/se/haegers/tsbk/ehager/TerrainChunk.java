package se.haegers.tsbk.ehager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class TerrainChunk extends BoxShaped
{
	/**
	 * A chunk of renderable terrain. Part of a MarchedField.
	 * 
	 */
	public TerrainChunk(MarchedField parent, int side, int xOff, int yOff, int zOff)
	{
		super(side*parent.getSpacing(), side*parent.getSpacing(), side*parent.getSpacing());
		this.parent = parent;
		pointsPerSide = side;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		warpTo(parent.getSpacing()*(xOff+side/2),
				parent.getSpacing()*(yOff+side/2),
				parent.getSpacing()*(zOff+side/2));
	}
	
	/**
	 * Render the solid ground part of the chunk.
	 * Must call beginGroundRender first.
	 */
	public void renderGround()
	{
		if(groundMesh != null)
			groundMesh.render(terrainShader, GL20.GL_TRIANGLES);
	}
	
	public void renderWater()
	{
		if(waterMesh != null)
			waterMesh.render(waterShader, GL20.GL_TRIANGLES);
	}
	
	public boolean pointsIncluded(TerrainPoint[] points)
	{	
		for(int q=0; q < points.length; ++q)
		{
			if(isInside(new Vector3(points[q].getCoordsArr())))
				return true;
		}
		
		return false;
	}
	
	public void refreshWaterMesh()
	{
		parent.refreshWaterNormals(xOff-1, yOff-1, zOff-1,
			xOff+pointsPerSide+1, yOff+pointsPerSide+1, zOff+pointsPerSide+1);
		
		parent.makeWaterEdges(xOff, xOff+pointsPerSide, 
				yOff, yOff+pointsPerSide,
				zOff, zOff+pointsPerSide);
		
		waterMesh = parent.getCurrentMesh();
	}
	
	public void refreshSolidMesh()
	{	
		parent.refreshSolidNormals(xOff-1, yOff-1, zOff-1,
			xOff+pointsPerSide+1, yOff+pointsPerSide+1, zOff+pointsPerSide+1);
		
		parent.makeSolidEdges(xOff, xOff+pointsPerSide, 
							yOff, yOff+pointsPerSide, 
							zOff, zOff+pointsPerSide);
		
		groundMesh = parent.getCurrentMesh();
	}
	
	
	public static void setGroundShader(String vec, String frag)
	{
		terrainShader = new ShaderProgram(Gdx.files.internal(vec), Gdx.files.internal(frag));
		Gdx.app.log("Terrain", terrainShader.isCompiled() ? "Terrain compiled successfully" : terrainShader.getLog());
	}
	public static void beginGroundRender(Matrix4 projection)
	{
		terrainShader.begin();
		terrainShader.setUniformMatrix("u_projection", projection);
	}
	public static void endGroundRender()
	{
		terrainShader.end();
	}
	public static void setWaterShader(String vec, String frag)
	{
		waterShader = new ShaderProgram(Gdx.files.internal(vec), Gdx.files.internal(frag));
		Gdx.app.log("Water", waterShader.isCompiled() ? "Water compiled successfully" : waterShader.getLog());
	}
	public static void beginWaterRender(Matrix4 projection)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		waterShader.begin();
		waterShader.setUniformMatrix("u_projection", projection);
	}
	public static void endWaterRender()
	{
		waterShader.end();
	}
	
	private Mesh groundMesh;
	private Mesh waterMesh;
	private MarchedField parent;
	private int pointsPerSide;
	private int xOff, yOff, zOff;
	
	private static ShaderProgram terrainShader;
	private static ShaderProgram waterShader;
}
