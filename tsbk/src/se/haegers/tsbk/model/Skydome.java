package se.haegers.tsbk.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Vector3;

public class Skydome implements ModelInterface {

	/*
	 * Source for sky dome model
	 * https://github.com/libgdx/libgdx/tree/master/tests/gdx-tests-android/assets/data/g3d
	 * More specifically:
	 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests-android/assets/data/g3d/skydome.g3db
	 */
	
	private static final double SUN_START = 0;
	private Model skydomeModel;
	private Model sunModel;
	private Shader skydomeShader;
	private Shader sunShader;
	private Environment environment;
	private Renderable domeRenderable;
	private RenderContext renderContext;
	private AssetManager assets;
	private boolean loading;
	private Renderable sunRenderable;
	private float simulationTime;
	private float simulationSpeed;
	private float sunX;
	private float sunY;
	
	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		skydomeShader = new SkydomeShader();
		skydomeShader.init();
		assets = new AssetManager();
		assets.load("data/skydome.g3db", Model.class);
		loading = true;
		
		sunShader = new SunShader();
		sunShader.init();
		ModelBuilder mb = new ModelBuilder();
		sunModel = mb.createSphere(10, 10, 10, 20, 20, new Material(), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		NodePart blockPart = sunModel.nodes.get(0).parts.get(0);
		sunRenderable = new Renderable();
		blockPart.setRenderable(sunRenderable);
		sunRenderable.environment = null;
		sunRenderable.worldTransform.idt().translate(new Vector3(0, 200, 0));
		
		renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
		
		setSimulationSpeed(1);
		
	}

	private void doneLoading() {
		skydomeModel = assets.get("data/skydome.g3db", Model.class);
		
		NodePart blockPart = skydomeModel.nodes.get(0).parts.get(0);
		domeRenderable = new Renderable();
		blockPart.setRenderable(domeRenderable);
		domeRenderable.environment = null;
		domeRenderable.worldTransform.idt();
//		domeRenderable.primitiveType = GL20.GL_POINTS;		// Uncomment for point cloud
//		domeRenderable.primitiveType = GL20.GL_LINES;		// or wire frame
		
		loading = false;
	}
	
	@Override
	public void render(Camera camera) {
		
		if(loading && assets.update())
			doneLoading();
		
		if(domeRenderable == null) {
			return;
		}
		
		simulationTime += Gdx.graphics.getDeltaTime();
		
		sunX = (float)(200*Math.cos(SUN_START + simulationTime*getSimulationSpeed()));
		sunY = (float)(200*Math.sin(SUN_START + simulationTime*getSimulationSpeed()));
		sunRenderable.worldTransform.setToTranslation(sunX, sunY, 0);
		
		renderContext.begin();
		skydomeShader.begin(camera, renderContext);
		skydomeShader.render(domeRenderable);
		skydomeShader.end();
		
		sunShader.begin(camera, renderContext);
		sunShader.render(sunRenderable);
		sunShader.end();
		renderContext.end();
	}

	@Override
	public void dispose() {
		skydomeModel.dispose();
		skydomeShader.dispose();
//		assets.dispose();
	}

	/**
	 * Gets the scalar of which is used in the sun's rotation simulation.
	 * @return A scalar. 1.0 means full speed.
	 */
	public float getSimulationSpeed() {
		return simulationSpeed;
	}

	/**
	 * Set the scalar which is used in the sun's rotation simulation.
	 * @param simulationSpeed 1.0 means full speed, 0.5 means half the speed, etc.
	 */
	public void setSimulationSpeed(float simulationSpeed) {
		this.simulationSpeed = simulationSpeed;
	}
	
	/**
	 * Gets the sun's world coordinates.
	 * @return a @Vector3 containing the sun's world coordinates.
	 */
	public Vector3 getSunPosition() {
		Vector3 v = new Vector3();
		sunRenderable.worldTransform.getTranslation(v);
		return v;
	}
	
	/**
	 * Gets the sun's angle vector towards the world.
	 * @return a normalized @Vector3 pointing in the sun's lighting direction. 
	 */
	public Vector3 getSunAngle() {
		Vector3 v = new Vector3(1, 0, 0);
		
		v.rotateRad(Vector3.Z, (float)Math.atan(sunY/sunX)).nor();
		if(getSunPosition().x > 0)
			v.scl(-1);
		
		return v;
	}

}
