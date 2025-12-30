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

import java.util.concurrent.TimeUnit;

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
    private FrameBuffer interFbo;

    private TextureRegion fboRegion;
    private TextureRegion fboFogRegion;
    private TextureRegion interFboRegion;
    private TextureRegion collisionFboRegion;

    private ShaderProgram fogShader;
    private ShaderProgram grayScaleShader;
    private ShaderProgram collisionShader;
    private ShaderProgram combinationShader;

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

        combinationShader = new ShaderProgram(
                Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/combination.frag")
        );

        ((OrthographicCamera)stage.getCamera()).zoom = 1f;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Create a cocllision FBO based on the collision layer
        collisionFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        interFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        interFboRegion = new TextureRegion(interFbo.getColorBufferTexture());
        interFboRegion.flip(false, true);

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
        stage.act(delta);

        Batch batch = stage.getBatch();
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();

        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float viewX = camera.position.x - viewW / 2f;
        float viewY = camera.position.y - viewH / 2f;

        // TODO Combine shaders
        fbo.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            mapRenderer.setView(camera);
            mapRenderer.render();
            stage.draw();
        fbo.end();

        fboFog.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            batch.setProjectionMatrix(camera.combined);
            batch.setShader(fogShader);

            batch.begin();
                fogShader.setUniformf("u_playerWorldPos", player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);
                fogShader.setUniformf("u_camWorldPos", camera.position.x, camera.position.y);
                fogShader.setUniformf("u_worldViewSize", viewW, viewH);
                fogShader.setUniformf("u_radiusWorld", fogIntensity);

                batch.draw(fboRegion, viewX, viewY, viewW, viewH);
            batch.end();
        fboFog.end();

        stage.getViewport().apply();

        interFbo.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            batch.setProjectionMatrix(camera.combined);
            batch.setShader(collisionShader);
            batch.begin();
                collisionFbo.getColorBufferTexture().bind(1);
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

                float uvWidth  = viewW / worldWidth;
                float uvHeight = viewH / worldHeight;
                float uvStartX = viewX / worldWidth;
                float uvStartY = viewY / worldHeight;

                collisionShader.setUniformi("u_mask", 1);
                collisionShader.setUniformf("u_uvOffset", uvStartX, uvStartY);
                collisionShader.setUniformf("u_uvScale", uvWidth, uvHeight);

                batch.draw(fboRegion,viewX, viewY, viewW, viewH);

                System.out.printf("FBORegion: %s:%s%n",fboRegion.getRegionWidth(), fboRegion.getRegionHeight());
            batch.end();
        interFbo.end();

        stage.getViewport().apply();

        ScreenUtils.clear(0, 0, 0, 1);
        batch.setProjectionMatrix(camera.combined);
        batch.setShader(combinationShader);
        batch.begin();
            fboFog.getColorBufferTexture().bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            combinationShader.setUniformi("u_mask", 1);

            batch.draw(interFboRegion, viewX, viewY, viewW, viewH);

            System.out.printf("fboFog: %s:%s%n",fboFog.getColorBufferTexture().getWidth(), fboFog.getColorBufferTexture().getHeight());
            System.out.printf("%s  %s  %s %s %n",
                    stage.getCamera().position.x - stage.getCamera().viewportWidth/2,
                    stage.getCamera().position.y - stage.getCamera().viewportHeight/2,
                    stage.getViewport().getWorldWidth(),
                    stage.getViewport().getWorldHeight()
            );
            System.out.printf("interFboRegion: %s:%s%n",interFboRegion.getRegionWidth(), interFboRegion.getRegionHeight());

        batch.end();
        batch.setShader(null);
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
            collisionSR.setColor(Color.WHITE);
            collisionSR.rect(0,0, width, height);
            collisionSR.setColor(new Color(0.3f, 0.3f, 0.3f, 1));

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
