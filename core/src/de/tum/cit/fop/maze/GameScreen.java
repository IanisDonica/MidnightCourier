package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputEventQueue;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;
    private int score = 0;
    public final int worldWidth = 32;
    public final int worldHeight = 16;
    private float fogIntensity = 2.5f;
    private boolean noireMode = true;

    private FrameBuffer fbo;
    private FrameBuffer fboFog;

    private TextureRegion fboRegion;
    private TextureRegion fboFogRegion;

    private ShaderProgram fogShader;
    private ShaderProgram grayScaleShader;

    private Player player;
    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        Viewport viewport = new FitViewport(worldWidth, worldHeight);
        stage = new Stage(viewport, game.getSpriteBatch());
        map = new TmxMapLoader().load("untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1/16f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);

        fboFog = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboFogRegion = new TextureRegion(fboFog.getColorBufferTexture());
        fboFogRegion.flip(false, true);

        fogShader = new ShaderProgram(
                Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/fog.frag")
        );

        grayScaleShader = new ShaderProgram(
                Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/grayscale.frag")
        );

        // Since its a FitViewport the aspect ratio should stay the same ( I think ), thus no need to eevr update it again
        //fogShader.setUniformf("u_aspectRatio", (float) worldWidth / worldHeight);

        fogShader.bind();
        ((OrthographicCamera)stage.getCamera()).zoom = 1f;

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.goToMenu();
                    return true;
                }
                if (keycode == Input.Keys.NUMPAD_ADD) {
                    ((OrthographicCamera)stage.getCamera()).zoom += 0.1f;
                }
                if (keycode == Input.Keys.NUMPAD_SUBTRACT) {
                    ((OrthographicCamera)stage.getCamera()).zoom -= 0.1f;
                }
                if (keycode == Input.Keys.NUMPAD_9) {
                    fogIntensity += 0.2f;
                }
                if (keycode == Input.Keys.NUMPAD_8) {
                    fogIntensity -= 0.2f;
                }
                if (keycode == Input.Keys.NUMPAD_7) {
                    noireMode = !noireMode;
                }
                return false;
            }
        });
    }


    @Override
    public void render(float delta) {
        fbo.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            stage.getViewport().apply();

            mapRenderer.setView((OrthographicCamera) stage.getCamera());
            mapRenderer.render();

            stage.act(delta);

            stage.draw();
        fbo.end();

        Batch batch = stage.getBatch();
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();

        //This should be needed but the code without it, wtf?
        Matrix4 oldMatrix = batch.getProjectionMatrix().cpy();
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        fboFog.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            batch.setShader(fogShader);

            batch.begin();

                fogShader.setUniformf("u_playerWorldPos", player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);
                fogShader.setUniformf("u_camWorldPos", camera.position.x, camera.position.y);
                fogShader.setUniformf("u_worldViewSize", camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
                fogShader.setUniformf("u_radiusWorld", fogIntensity);

                batch.draw(fboRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
        fboFog.end();

        if (noireMode) { batch.setShader(grayScaleShader); }

        batch.begin();
            batch.draw(fboFogRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        batch.setShader(null);
        batch.setProjectionMatrix(oldMatrix);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        player = new Player(map, 16, 8);

        stage.addActor(player);
        stage.setKeyboardFocus(player);

        stage.getCamera().position.set(16 + player.getWidth() / 2, 8 + player.getHeight() / 2, 0);
        stage.getCamera().update();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    // Additional methods and logic can be added as needed for the game screen
}
