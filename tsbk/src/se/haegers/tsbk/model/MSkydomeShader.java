package se.haegers.tsbk.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MSkydomeShader implements Shader {
	
	private ShaderProgram program;
	private RenderContext context;
	private Camera camera;

	@Override
	public void init() {
		program = new ShaderProgram(Gdx.files.internal("shaders/mSkydomeVert.glsl"), 
									Gdx.files.internal("shaders/mSkydomeFrag.glsl"));
		
		if(!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());

	}
	
	@Override
	public void dispose() {
		program.dispose();

	}

	@Override
	public int compareTo(Shader other) {
		// Not used
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}

	@Override
	public void begin(Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		program.begin();
		program.setUniformMatrix("u_combinedMat", camera.combined);
//		context.setDepthTest(GL20.GL_LEQUAL);	// TODO Is this the right parameter?
		context.setCullFace(GL20.GL_BACK);
	}

	@Override
	public void render(Renderable renderable) {
		program.setUniformMatrix("u_worldMat", renderable.worldTransform);
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
	}

	@Override
	public void end() {
		program.end();
	}

}
