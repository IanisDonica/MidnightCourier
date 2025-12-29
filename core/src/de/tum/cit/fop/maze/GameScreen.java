package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputEventQueue;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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
    private final TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;
    public final int worldWidth = 32;
    public final int worldHeight = 16;
    private float fogIntensity = 4f;
    private boolean noireMode = false;

    private FrameBuffer fbo;
    private FrameBuffer fboFog;
    private FrameBuffer collisionFbo;
    private ShapeRenderer collisionSR;

    private TextureRegion fboRegion;
    private TextureRegion fboFogRegion;
    private TextureRegion collisionFboRegion;

    private ShaderProgram fogShader;
    private ShaderProgram grayScaleShader;
    private ShaderProgram collisionShader;

    private OrthographicCamera uiCamera;

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

        collisionShader = new ShaderProgram(
                Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/collision.frag")
        );

        ((OrthographicCamera)stage.getCamera()).zoom = 1f;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Create a cocllision FBO based on the collision layer
        collisionFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        generateFBO();

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
                    fogIntensity += 0.5f;
                }
                if (keycode == Input.Keys.NUMPAD_8) {
                    fogIntensity -= 0.5f;
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
        // TODO Combine shaders
        fbo.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            mapRenderer.setView((OrthographicCamera) stage.getCamera());
            mapRenderer.render();

            stage.act(delta);
            stage.draw();
        fbo.end();


        Batch batch = stage.getBatch();
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();

        fboFog.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            batch.setProjectionMatrix(uiCamera.combined); // Easier than having to constantly update the Projection Matrices
            batch.setShader(fogShader);

            batch.begin();
                fogShader.setUniformf("u_playerWorldPos", player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);
                fogShader.setUniformf("u_camWorldPos", camera.position.x, camera.position.y);
                fogShader.setUniformf("u_worldViewSize", camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
                fogShader.setUniformf("u_radiusWorld", fogIntensity);

                batch.draw(fboRegion, 0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight);
            batch.end();
        fboFog.end();

        stage.getViewport().apply();
        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);

        //if (noireMode) { batch.setShader(grayScaleShader); }
        batch.setShader(collisionShader);

        batch.begin();
            ScreenUtils.clear(0, 0, 0, 1);

            collisionFbo.getColorBufferTexture().bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

            float uvWidth = camera.viewportWidth * camera.zoom / worldWidth;
            float uvHeight = camera.viewportHeight * camera.zoom / worldHeight;

            float uvStartX = (camera.position.x - (camera.viewportWidth * camera.zoom / 2f)) / worldWidth;
            float uvStartY = (camera.position.y - (camera.viewportHeight * camera.zoom / 2f)) / worldHeight;

            collisionShader.setUniformi("u_mask", 1);
            collisionShader.setUniformf("u_uvOffset", uvStartX, uvStartY);
            collisionShader.setUniformf("u_uvScale", uvWidth, uvHeight);

            batch.draw(fboFogRegion,
                    stage.getCamera().position.x - stage.getCamera().viewportWidth/2,
                    stage.getCamera().position.y - stage.getCamera().viewportHeight/2,
                    stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight()
            );
        batch.end();

        batch.setShader(null);
        /*

        Batch batch = stage.getBatch();

        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.setShader(collisionShader);

        batch.begin();
            ScreenUtils.clear(0, 0, 0, 1);

            collisionFbo.getColorBufferTexture().bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            collisionShader.setUniformi("u_mask", 1);

            batch.draw(fboRegion,0,0, worldWidth, worldHeight);

        batch.end();


        stage.getViewport().apply();
        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.setShader(null); */
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
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

    private void generateFBO() {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("Walls");
        int height = collisionLayer.getHeight();
        int width = collisionLayer.getWidth();
        collisionSR = new ShapeRenderer();
        collisionFbo.begin();

            collisionSR.setProjectionMatrix(stage.getCamera().combined);
            collisionSR.begin(ShapeRenderer.ShapeType.Filled);
            collisionSR.setColor(new Color(0.5f, 0.5f, 0.5f, 1));

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        if (collisionLayer.getCell(j, i) != null) {
                            collisionSR.rect(j,i, 1, 1);
                        }
                    }
                }

            collisionSR.end();
        collisionFbo.end();

    }
}
