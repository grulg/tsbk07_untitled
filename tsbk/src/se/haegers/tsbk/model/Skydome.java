package se.haegers.tsbk.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Skydome implements Model {
	
	private int resolution;
	
	private float verticalSweep;
	
	private float radius;
	
	private float heightScale;
	
	private Vector3 origin;
	
	private Mesh mesh;
	
	private Vector3 baseDayAmbient;
	
	private Vector3 baseNightAmbient;
	
	private Vector3 baseDayColor;
	
	private Vector3 baseNightColor;
	
	private Vector3 currentSunNormal;
	
	private Vector3 currentAmbient;
	
	private Vector3 currentSkyColor;
	
	private ShaderProgram shader;
	
	
	public Skydome(int resolution, float verticalSweep, float radius, float heightScale,
			Vector3 origin, Vector3 dayAmbient, Vector3 nightAmbient, Vector3 dayColor, 
			Vector3 nightColor, ShaderProgram shader) {
		
		// Validation
		assert (resolution > 0 && radius > 0);
		assert (verticalSweep > 0 && verticalSweep <= 90);
		
		
		this.resolution = resolution;
		this.verticalSweep = verticalSweep;
		this.radius = radius;
		this.heightScale = heightScale;
		this.origin = origin;
		this.baseDayAmbient = dayAmbient;
		this.baseDayColor = dayColor;
		this.baseNightAmbient = nightAmbient;
		this.baseNightColor = nightColor;
		this.shader = shader;
		
		// Calculate number of vertices and prepare the mesh
		int numberOfVertices = 1 + 4 * resolution * resolution;
		this.mesh = new Mesh(true, numberOfVertices, 0, new VertexAttribute(Usage.Position, 3, "a_position"));
		
		// Adjust the radius based on the vertical sweep
		double radAngle = (90 - verticalSweep) / 180 * Math.PI;
		radius /= Math.cos(radAngle);
		
		// Compute the z adjustment (amount of which the radius is reduced at top?)
		float zAdjust = (float) (radius * Math.sin(radAngle));
		
		// Calculate the horizontal sweep of one section of the dome
		// based on the resolution 
		float horizontalSweep = 90f / resolution;
		
		// Adjust with the given resolution
		this.verticalSweep /= resolution;
		
		float[] vertices = new float[numberOfVertices];
		
		// Start the array with the vertex at the top of the dome
		vertices[0] = origin.x;
		vertices[1] = origin.y;
		vertices[2] = ((radius + zAdjust) * heightScale) + origin.z;
		
		
		int currentVertexIndex = 3;
		for (int i = 0; i < resolution; i++) {
			
			// Compute the vertex that will be rotated around to make a ring
			
			// Place the vertex at the outer rim on the base
			Vector3 vertex = new Vector3(0, 0, radius);
			Matrix3 m = new Matrix3();
			
			// Rotate the vertex "up", revolving around the X-axis
			m.setToRotation(Vector3.X, verticalSweep * (i + 1));
			vertex.mul(m);
//			assert vertex.z > (zAdjust - ON_EPSILON)
			vertex.z = (vertex.z - zAdjust) * heightScale;
			
			
			Vector3 tmp;
			for (int j = 0; j < resolution; j++) {
				
				// Loop through the ring
				
				m.setToRotation(Vector3.Z, horizontalSweep * j);
				tmp = new Vector3(vertex);
				tmp.mul(m);
				tmp.add(origin);
				
				vertices[currentVertexIndex] = tmp.x;
				vertices[currentVertexIndex + 1] = tmp.y;
				vertices[currentVertexIndex + 2] = tmp.z;
				
				currentVertexIndex += 3;
			}
		}
		
		mesh.setVertices(vertices);
		
		float[] v = new float[mesh.getNumVertices()];
		mesh.getVertices(v);
		System.out.println(mesh.getNumVertices());
		for(int i = 0; i < v.length; ++i) {
			
			System.out.print(v[i]);
			
			if((i > 0) && ((i % 3) == 0))
				System.out.println();
			else
				System.out.print(", ");
		}
	}

	@Override
	public void load() {
		
	}

	@Override
	public void render(Matrix4 cameraMatrix) {
		GL20 gl = Gdx.gl20;
		
		gl.glEnable(GL20.GL_BLEND);
		gl.glDepthMask(false);
		
		shader.begin();
		shader.setUniformMatrix("u_combinedMat", cameraMatrix);
		mesh.render(shader, GL20.GL_LINES);
		shader.end();
		
		gl.glDisable(GL20.GL_BLEND);
		gl.glDepthMask(true);
	}

	@Override
	public void dispose() {
		shader.dispose();		
	}
	
}
