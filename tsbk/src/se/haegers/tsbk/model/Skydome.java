package se.haegers.tsbk.model;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Skydome implements ModelInterface {
	
	
	/**
	 * Number of segments in a 90 degree slice horizontally and vertically.
	 */
	private int resolution;
	
	
	/**
	 * The range which is to be included in the dome. >0 and <= 90.
	 */
	private float verticalSweep;
	
	/**
	 * Radius at the base of the dome.
	 */
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
		
		// Compute the y adjustment
		float yAdjust = (float) (radius * Math.sin(radAngle));
		
		// Calculate the horizontal sweep of one section of the dome
		// based on the resolution 
		float horizontalSweep = 360f / resolution;
		
		// Adjust with the given resolution
		this.verticalSweep /= resolution;
		
		ArrayList<Float> vertices = new ArrayList<Float>();
		
		// Start the array with the vertex at the top of the dome
		vertices.add(origin.x);
		vertices.add(((radius - yAdjust) * heightScale) + origin.y);
		vertices.add(origin.z);
		
		for (int i = 0; i < resolution; i++) {
			
			// Compute the vertex that will be rotated around to make a ring
			
			// Place the vertex at the top
			Vector3 vertex = new Vector3(0, radius, 0);
			Matrix4 m = new Matrix4();
			
			// Rotate the vertex "down" from the top, revolving around the X-axis
			m.setToRotation(Vector3.X, verticalSweep * (i + 1));
			vertex.mul(m);
//			assert vertex.z > (zAdjust - ON_EPSILON)
			vertex.y = (vertex.y - yAdjust) * heightScale;
			
			
			Vector3 tmp;
			for (int j = 0; j < resolution; j++) {
				
				// Loop through the ring
				Matrix4 n = new Matrix4();
				n.setToRotation(Vector3.Y, horizontalSweep * j);
				tmp = new Vector3(vertex);
				tmp.mul(n);
				tmp.add(origin);
				
				vertices.add(tmp.x);
				vertices.add(tmp.y);
				vertices.add(tmp.z);
			}
		}
		
		float[] v = toFloatArray(vertices);
		mesh.setVertices(v);
		
//		float[] v = new float[mesh.getNumVertices()];
//		mesh.getVertices(v);
		System.out.println("Calculated vertices: " + numberOfVertices);
		System.out.println("Mesh vertices: " + mesh.getNumVertices());
		System.out.println("V.length / 3 = " + v.length / 3);
//		for(int i = 0; i < v.length; ++i) {
//			
//			System.out.print(String.format("%.5g", v[i]));
//			
//			if((i > 0) && ((i % 3) == 2))
//				System.out.println();
//			else
//				System.out.print(", ");
//		}
	}

	private float[] toFloatArray(ArrayList<Float> vertices) {
		
		float[] a = new float[vertices.size()];
		
		for(int i = 0; i < vertices.size(); ++i) {
			a[i] = vertices.get(i);
		}
		
		return a;
	}

	@Override
	public void create() {
		
	}

	@Override
	public void render(Camera camera) {
		GL20 gl = Gdx.gl20;
		Matrix4 cameraMatrix = camera.combined;
		
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
