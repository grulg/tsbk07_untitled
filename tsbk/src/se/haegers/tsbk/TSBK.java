package se.haegers.tsbk;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import se.haegers.tsbk.ehager.HeightMap;
import se.haegers.tsbk.ehager.NoiseMap;
import se.haegers.tsbk.model.MSkydome;
import se.haegers.tsbk.model.Skydome;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
/**
 * Current controls:
 * Camera movement in X and Y directions: W,A,S,D
 * Camera movement in Z direction: Scroll Wheel
 * Camera Rotation: Hold down Left Mouse Button and move mouse around
 * 
 * @author Emil
 *
 */
public class TSBK implements ApplicationListener, InputProcessor {
	/*
	 * Constants used for navigation. Change them to suit your needs!
	 */
	private final float ZOOM_SPEED = 0.5f;
	private final float CAMERA_MOVEMENT_SPEED = 0.1f;
	private final float CAMERA_ROTATE_SPEED = 2.0f;

	/*
	 * Objects used for projection, drawing and getting text on the screen.
	 */
	private PerspectiveCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	private BitmapFont font;
	private Mesh mesh;
	
	/*
	 * Object used for customizable shaders. Takes a vertex- and fragment-shader
	 * in the initialize method.
	 */
	ShaderProgram makeRedShader;
	
	/*
	 * Variables used for controlling whether the mouse should rotate the view
	 * (hold down the left mouse button and move the mouse to see it's use).
	 */
	private int screen_x = 0, screen_y = 0;
	private boolean leftPressed = false;

	/*
	 * A simple enumeration and map to keep track of whether a button is pressed down or not.
	 */
	private enum Move_Buttons {
		CAM_LEFT, 
		CAM_RIGHT, 
		CAM_UP, 
		CAM_DOWN,
		CAM_FORWARD
	}
	
	static Map<Move_Buttons, Boolean> buttons = new HashMap<Move_Buttons, Boolean>();
	static {
		buttons.put(Move_Buttons.CAM_LEFT, false);
		buttons.put(Move_Buttons.CAM_RIGHT, false);
		buttons.put(Move_Buttons.CAM_UP, false);
		buttons.put(Move_Buttons.CAM_DOWN, false);
		buttons.put(Move_Buttons.CAM_FORWARD, false);
	}; 
	
	/*
	 * Tholin's variables
	 */
	@SuppressWarnings("unused")
	private SkeletonRendererDebug debugRenderer;
	private SkeletonRenderer skeletonRenderer;
	private TextureAtlas atlas;
	private Skeleton skeleton;
	private AnimationState animationState;
	private Bone root;
	float time = 0;

	/*
	 * Haeger's variables
	 */
	private Skydome skydome;
	private MSkydome mSkydome;

	/*
	 * Emil's variables
	 */
	
	
	@Override
	public void create() {
		/*
		 * Gdx.graphics contains all graphical info, even accessible at run time.
		 * Gets the screen width and height.
		 */
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		/*
		 * Sets up the perspective camera with 67 degree field of view (standard, don't know it's use)
		 * and a near field of width w and height h (recall the viewing frustum).
		 * IMPORTANT! The camera needs to be updated for a change to take place, as the translation
		 * here for example.
		 */
		camera = new PerspectiveCamera(67, w, h);
		camera.translate(new Vector3(0,0,2));
		camera.update();
		batch = new SpriteBatch();
		
		/*
		 * The texture constructor wants a path to the actual image, which starts in the assets folder
		 * of the Android-project.
		 * Linear, Linear is a standard, cool filter effect (recall the lecture).
		 * Bind it to the texture unit 0.
		 */
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
		texture = new Texture(Gdx.files.internal("data/libgdx.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		texture.bind();
		
		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
		
		// Straight forward jazz.
		sprite = new Sprite(region);
		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
		
		
		ShaderProgram.pedantic = false; // Uniforms and attributes doesn't have to be present at all times => more efficient.
		/*
		 * The shader program contructor takes the path to the shaders on disk (starts at Android project, assets folder)
		 * Makes a log entry whether the compilation was successful or not.
		 */
		makeRedShader = new ShaderProgram(Gdx.files.internal("shaders/makeRed.vsh"), Gdx.files.internal("shaders/makeRed.fsh"));
		Gdx.app.log("makeRed", makeRedShader.isCompiled() ? "makeRed compiled successfully" : makeRedShader.getLog());
		
		/*
		 * The mesh is static (true), max 4 vertices, and we want it to store information about position with 3
		 * positional values (x,y,z) per vetex and 2 texture coordinates (x,y), and we send it to the shaders 
		 * as "a_position" and "a_texCoord0". 
		 */	
		mesh = new Mesh(true, 4, 0, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		
		mesh.setVertices( new float[] { -0.5f, -1.0f, 0, 0, 1,
                						-0.5f, -0.5f, 0, 0, 0,
                						0.0f, -0.5f, 0, 1, 0,
                						0.0f, -1.0f, 0, 1, 1 } );
		
		/*
		 * This font is used for simple debug display. The default BitmapFont-constructor
		 * gives 15pt Arial, but it is possible to make your own.
		 */
		font = new BitmapFont();
		
		/*
		 * Tell Lib GDX that this object shall be the object that handles input from mouse/keyboard
		 * (you can multi-plex it so that any objects can control it if you like, but I don't think we will need that)
		 */
		Gdx.input.setInputProcessor(this);
		
		/*
		 * Just expand our own create for now, so we won't have an issue with merging later.
		 */
		tholinCreate();
		haegerCreate();
		emilCreate();
	}
	
	private void tholinCreate() {
		debugRenderer = new SkeletonRendererDebug();
    	skeletonRenderer = new SkeletonRenderer();
    	
		atlas = new TextureAtlas(Gdx.files.internal("data/speedy.atlas"));
    	SkeletonJson json = new SkeletonJson(atlas);
    	SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("data/speedy.json"));
    	
    	skeleton = new Skeleton(skeletonData);
    	
    	animationState = new AnimationState(new AnimationStateData(skeletonData));
    	animationState.setAnimation(0, "run", true);
    	
    	root = skeleton.getRootBone();
    	root.setScaleX(0.002f);
    	root.setScaleY(0.002f);
    	root.setX(-1);
    	root.setY(-0.3f);
		
		skeleton.updateWorldTransform();
		
	}

	private void haegerCreate() {
		
//		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/skydomeVert.glsl"), 
//				Gdx.files.internal("shaders/skydomeFrag.glsl"));
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/wireframeSkydomeVert.glsl"), 
				Gdx.files.internal("shaders/wireframeSkydomeFrag.glsl"));
		Gdx.app.log("skydome", shader.isCompiled() ? "skydome compiled successfully" : shader.getLog());
		
		this.skydome = new Skydome(
				100,									// Resolution
				55f,								// Vertical Sweep (degrees)
				50,								// Radius
				1.0f,								// Height Scale
				new Vector3(0, 0, 0),				// Offset
				new Vector3(1.0f, 1.0f, 1.0f),		// Base day light ambient color
				new Vector3(0.4f, 0.4f, 0.4f),		// Base Night light ambient color
				new Vector3(0.25f, 0.31f, 0.63f),	// Base day sky color
				new Vector3(0.1f, 0.1f, 0.11f),		// Base night sky color
				shader
		);
		
		this.mSkydome = new MSkydome(shader);
		this.mSkydome.create();
		
	}

	private void emilCreate() 
	{
		
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
		makeRedShader.dispose();
		
		atlas.dispose();
	}

	@Override
	public void render() {
		/*
		 * Clear the screen to black. Looks friggin' delicious, like the C-labs.
		 */
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		updateCamera();
		
		/*
		 * Beautiful texture renderer abstraction. Draw everything to a batch, and then throw it on the screen.
		 */
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		sprite.draw(batch);
		batch.end();
		
		/*
		 * Same as above but with our custom shader. More work for the same goal, but is needed when we want
		 * flashier stuff.
		 */
		makeRedShader.begin();
		makeRedShader.setUniformi("u_texture", 0);
		makeRedShader.setUniformMatrix("u_combinedMat", camera.combined);
		mesh.render(makeRedShader, GL20.GL_TRIANGLE_FAN);
		makeRedShader.end();
		
		/*
		 * Just expand our own draw for now, so we won't have an issue with merging later.
		 */
		tholinDraw();
		haegerDraw(camera);
		emilDraw();

		/*
		 * Finally, since the UI shall not be affected by position in the world, we simply cancel that out
		 * (the code simply makes it so we draw the rest of the stuff on the near plane directly)
		 */
		Matrix4 uiMatrix = camera.combined.cpy();
		uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setProjectionMatrix(uiMatrix);
		
		batch.begin();
		// Cool function for multi-line text. Just use \n.
		font.drawMultiLine(batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + 
				"\nCamera Position: " + (int) camera.position.x + ", " + (int) camera.position.y + ", " + (int) camera.position.z +
				"\nCamera Up-Vector: " + camera.up + "\nCamera Direction: " + camera.direction, 10, Gdx.graphics.getHeight() - 10);
		batch.end();

	}
	
	private void tholinDraw() {
		float delta = Gdx.graphics.getDeltaTime();
		time += delta;
		
		animationState.apply(skeleton);
    	animationState.update(delta);
    	
		skeleton.updateWorldTransform();
		skeleton.update(delta);

		batch.begin();

		skeletonRenderer.draw(batch, skeleton);
    	
    	batch.end();	
	}

	private void haegerDraw(Camera camera) {
		mSkydome.render(camera);
	}

	private void emilDraw() {

	}
	
    public void updateCamera() {
    	/*
    	 * When we want to move right we simply cross the up- and direction-vector of the frustum
    	 * and normalize it, and than scale it with the camera movement speed. Simple.
    	 */
    	if (buttons.get(Move_Buttons.CAM_LEFT)) {
    		camera.translate(camera.up.cpy().crs(camera.direction).nor().scl(CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		if (buttons.get(Move_Buttons.CAM_RIGHT)) {
			camera.translate(camera.up.cpy().crs(camera.direction).nor().scl(-CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		/*
    	 * When we want to move up we simply use the up vector. Sweet!
    	 */
		if (buttons.get(Move_Buttons.CAM_UP)) {
			camera.translate(camera.up.cpy().nor().scl(CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		if (buttons.get(Move_Buttons.CAM_DOWN)) {
			camera.translate(camera.up.cpy().nor().scl(-CAMERA_MOVEMENT_SPEED));
			camera.update();
		}
		if (buttons.get(Move_Buttons.CAM_FORWARD)) {
			camera.translate(camera.direction.cpy().nor().scl(ZOOM_SPEED));
			camera.update();
		}
		if(leftPressed) {
			int x = Gdx.input.getX();
			int y = Gdx.input.getY();
			int deltaX = screen_x - x;
			int deltaY = screen_y - y;
			
			/*
			 * If the mouse has moved to the right, we simply rotate the camera to the right around the camera position
			 * with the up-vector as rotation axis.
			 */
			if(deltaX > 0) {
				camera.rotateAround(camera.position, camera.up, CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			else if(deltaX < 0) {
				camera.rotateAround(camera.position, camera.up, -CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			
			/*
			 * If the mouse moved up, we rotate the camera up by crossing the up vector with the direction vector to
			 * get the axis we want to rotate around (and finally normalize it), and we use camera position 
			 * as rotation point as before.
			 */
			if(deltaY > 0) {
				camera.rotateAround(camera.position, camera.up.cpy().crs(camera.position).nor(), CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			else if(deltaY < 0) {
				camera.rotateAround(camera.position, camera.up.cpy().crs(camera.position).nor(), -CAMERA_ROTATE_SPEED);
				camera.update(); 
			}
			
			// Set the current mouse position as previous mouse position, so that we can use it in the next iteration
			screen_x = x;
			screen_y = y;
		}
    }

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Keys.A) {
			buttons.get(buttons.put(Move_Buttons.CAM_LEFT, true));
		}
		if(keycode == Keys.D) {
			buttons.get(buttons.put(Move_Buttons.CAM_RIGHT, true));
		}
		if(keycode == Keys.W) {
			buttons.get(buttons.put(Move_Buttons.CAM_UP, true));
		}
		if(keycode == Keys.S) {
			buttons.get(buttons.put(Move_Buttons.CAM_DOWN, true));
		}
		if(keycode == Keys.F) {
			buttons.get(buttons.put(Move_Buttons.CAM_FORWARD, true));
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.A) {
			buttons.get(buttons.put(Move_Buttons.CAM_LEFT, false));
		}
		if(keycode == Keys.D) {
			buttons.get(buttons.put(Move_Buttons.CAM_RIGHT, false));
		}
		if(keycode == Keys.W) {
			buttons.get(buttons.put(Move_Buttons.CAM_UP, false));
		}
		if(keycode == Keys.S) {
			buttons.get(buttons.put(Move_Buttons.CAM_DOWN, false));
		}
		if(keycode == Keys.F) {
			buttons.get(buttons.put(Move_Buttons.CAM_FORWARD, false));
		}
		
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(button == Buttons.LEFT) {
			leftPressed = false;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(pointer == Buttons.LEFT) {
			leftPressed = true;
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		/*
		 * Moving forward simply means following the direction vector with a predetermined scale.
		 */
		camera.translate(camera.direction.cpy().nor().scl(-amount * ZOOM_SPEED));
		camera.update();
		return false;
	}
}
