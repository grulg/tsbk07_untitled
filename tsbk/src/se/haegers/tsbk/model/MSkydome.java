package se.haegers.tsbk.model;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
	private ModelInstance instance;
	private ArrayList<ModelInstance> instances;
	private ModelBatch modelBatch;
	private DefaultShader shader;
	private ShaderProgram shaderProgram;
	private Environment environment;
	
	public MSkydome(ShaderProgram s) {
		this.shaderProgram = s;
	}
	
	
	@Override
	public void create() {
		G3dModelLoader loader = new G3dModelLoader(new JsonReader());
		model = new Model(loader.loadModelData(Gdx.files.internal("data/sphere.g3dj"), null));
		
		instance = new ModelInstance(model);
		
		instances = new ArrayList<ModelInstance>();
		instances.add(instance);
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		modelBatch = new ModelBatch();
		
		Renderable r = new Renderable();
		instance.getRenderable(r);
		r.environment = environment;
		r.worldTransform.idt(); // TODO Här är du. Den rederar, men ser lite cp. Kanske gå igenom libgdx inlägget om shaders noggrannare och göra en egen shader implementation.
		
		shader = new DefaultShader(r);
		shader.program = shaderProgram;
		
		shader.init();
		
	}

	@Override
	public void render(Camera camera) {
		modelBatch.begin(camera);
		modelBatch.render(instances, shader);
		modelBatch.end();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();
		shader.dispose();
	}

}
