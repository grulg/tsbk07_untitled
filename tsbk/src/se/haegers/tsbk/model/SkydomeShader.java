package se.haegers.tsbk.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SkydomeShader implements Shader {
	
	private static final int TEXTURE_UNIT = 10;
	private ShaderProgram program;
	private Texture texture;

	@Override
	public void init() {
		program = new ShaderProgram(Gdx.files.internal("shaders/skydomeVert.glsl"), 
									Gdx.files.internal("shaders/skydomeFrag.glsl"));
		
		texture = new Texture(Gdx.files.internal("data/skydome1.png"));
		
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
		program.begin();
		texture.bind(TEXTURE_UNIT);
		program.setUniformMatrix("u_combinedMat", camera.combined);
		program.setUniformi("u_texture", TEXTURE_UNIT);
		
//		context.setDepthTest(GL20.GL_LEQUAL);	// TODO Is this the right parameter?
		context.setCullFace(GL20.GL_BACK);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}

	@Override
	public void render(Renderable renderable) {
		program.setUniformMatrix("u_worldMat", renderable.worldTransform);
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
	}

	@Override
	public void end() {
		program.end();
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	}

}
