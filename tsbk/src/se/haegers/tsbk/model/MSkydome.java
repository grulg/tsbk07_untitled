package se.haegers.tsbk.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.utils.JsonReader;

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
	
	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		G3dModelLoader loader = new G3dModelLoader(new JsonReader());
		model = new Model(loader.loadModelData(Gdx.files.internal("data/sphere.g3dj"), null));
		
		
		NodePart blockPart = model.nodes.get(0).parts.get(0);
		
		renderable = new Renderable();
		blockPart.setRenderable(renderable);
//		renderable.environment = environment;
		renderable.environment = null;					// TODO Här är du. Normaler åt fel håll?
		renderable.worldTransform.idt();
//		renderable.primitiveType = GL20.GL_POINTS;		// Uncomment for point cloud
//		renderable.primitiveType = GL20.GL_LINES;		// or wire frame
		
		renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
		shader = new MSkydomeShader();
		shader.init();
	}

	@Override
	public void render(Camera camera) {
		renderContext.begin();
		shader.begin(camera, renderContext);
		shader.render(renderable);
		shader.end();
		renderContext.end();
	}

	@Override
	public void dispose() {
		model.dispose();
		shader.dispose();
	}

}
