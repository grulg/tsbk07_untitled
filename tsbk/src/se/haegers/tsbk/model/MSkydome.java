package se.haegers.tsbk.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

public class MSkydome implements ModelInterface {

	/*
	 * Source for sky dome model
	 * https://github.com/libgdx/libgdx/tree/master/tests/gdx-tests-android/assets/data/g3d
	 * More specifically:
	 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests-android/assets/data/g3d/shapes/sphere.g3dj
	 * Should probably get the .g3db instead (it's binary => faster).
	 */
	
	private Model model;
	private Shader shader;
	private Environment environment;
	private Renderable renderable;
	private RenderContext renderContext;
	private AssetManager assets;
	private boolean loading;
	
	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		assets = new AssetManager();
		assets.load("data/skydome.g3db", Model.class);
		
		loading = true;
	}

	@Override
	public void render(Camera camera) {
		
		if(loading && assets.update())
			doneLoading();
		
		if(renderable == null) {
			return;
		}

		renderContext.begin();
		shader.begin(camera, renderContext);
		shader.render(renderable);
		shader.end();
		renderContext.end();
	}

	private void doneLoading() {
		model = assets.get("data/skydome.g3db", Model.class);
		
		NodePart blockPart = model.nodes.get(0).parts.get(0);
		renderable = new Renderable();
		blockPart.setRenderable(renderable);
		renderable.environment = null;
		renderable.worldTransform.idt();
//		renderable.primitiveType = GL20.GL_POINTS;		// Uncomment for point cloud
//		renderable.primitiveType = GL20.GL_LINES;		// or wire frame

		renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
		shader = new MSkydomeShader();
		shader.init();
		
		loading = false;
	}

	@Override
	public void dispose() {
		model.dispose();
		shader.dispose();
//		assets.dispose();
	}

}
