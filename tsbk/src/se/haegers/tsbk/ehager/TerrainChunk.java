package se.haegers.tsbk.ehager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
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
	public static void beginGroundRender(Matrix4 projection, float[] lDir)
	{
		groundTex.bind();
		terrainShader.begin();
		terrainShader.setUniform3fv("u_lDir", lDir, 0, 3);
		terrainShader.setUniformi("tex", 0);
		terrainShader.setUniformMatrix("u_projection", projection);
	}
	public static void endGroundRender()
	{
		terrainShader.end();
	}
	public static void setWaterShader(String vec, String frag)
	{
		waterShader = new ShaderProgram(Gdx.files.internal(vec), Gdx.files.internal(frag));
		Gdx.app.log("Water", waterShader.isCompiled() ? "Water compiled successfully!\n" + waterShader.getLog() : waterShader.getLog());
	}
	public static void beginWaterRender(Matrix4 projection, Matrix4 view, float[] lDir, Texture normalMapTex, Texture normalMapTex2, Texture waterTex, float elapsedTime)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		waterShader.begin();
		//waterShader.setUniformMatrix("u_projection", projection);
		
		normalMapTex.bind(2);
		normalMapTex2.bind(3);
		waterTex.bind(4);
		
		waterShader.setUniformMatrix("u_combinedMat", projection);//cameraController.getCamera().combined);
		waterShader.setUniformMatrix("u_modelViewMat", view);
		waterShader.setUniform3fv("u_lDir", lDir, 0, 3);
		waterShader.setUniformi("u_dudvTex", 2);
		waterShader.setUniformi("u_normalTex", 3);
		waterShader.setUniformi("u_waterTex", 4);
		waterShader.setUniformf("time", elapsedTime);
		
//		dudvMapTex.bind(1);
		//normalMapTex.bind(2);
	//	normalMapTex2.bind(3);
	//	waterTex.bind(4);
//		
		//waterProgram.setUniformMatrix("u_combinedMat", cameraController.getCamera().combined);//cameraController.getCamera().combined);
//		waterProgram.setUniformMatrix("u_modelViewMat", cameraController.getCamera().view);
//		waterProgram.setUniformi("u_dudvTex", 3);
//		waterProgram.setUniformi("u_normalTex", 2);
//		waterProgram.setUniformi("u_waterTex", 4);
//		waterProgram.setUniformf("time", elapsedTime);
//		waterMesh.render(waterProgram, GL20.GL_TRIANGLE_FAN);
		
	}
	public static void endWaterRender()
	{
		waterShader.end();
	}
	
	public static void loadGroundTexture(String path)
	{
		groundTex = new Texture(Gdx.files.internal(path));
		groundTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		groundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	}
	
	private Mesh groundMesh;
	private Mesh waterMesh;
	private MarchedField parent;
	private int pointsPerSide;
	private int xOff, yOff, zOff;
	
	private static Texture groundTex;
	private static ShaderProgram terrainShader;
	private static ShaderProgram waterShader;
}
