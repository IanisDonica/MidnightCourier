package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;
    public static final int WORLD_WIDTH = 32;
    public static final int WORLD_HEIGHT = 16;
    private float fogIntensity = 8f;
    private boolean noireMode = false;

    private FrameBuffer fbo;

    private TextureRegion fboRegion;
    private FrameBuffer noireFbo;
    private TextureRegion noireFboRegion;
    private FrameBuffer fboFog;
    private FrameBuffer interFbo;
    private TextureRegion interFboRegion;
    private FrameBuffer collisionFbo;

    private ShaderProgram grayScaleShader;
    private ShaderProgram fogShader;
    private ShaderProgram collisionShader;
    private ShaderProgram combinationShader;

    private OrthographicCamera uiCamera;

    private Player player;

    public void adjustZoom(float amount) {
        ((OrthographicCamera)stage.getCamera()).zoom += amount;
    }

    public void adjustFog(float amount) {
        fogIntensity += amount;
    }

    public void toggleNoireMode() {
        noireMode = !noireMode;
    }

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        map = new TmxMapLoader().load("untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1/16f, game.getSpriteBatch());
        createFrameBuffers(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        grayScaleShader = new ShaderProgram(
                Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/grayscale.frag")
        );

        fogShader = new ShaderProgram(
                Gdx.files.internal("shaders/vertex.glsl"),
                Gdx.files.internal("shaders/fog.frag")
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

        generateFBO();
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

        renderWorld(batch, camera, viewX, viewY, viewW, viewH);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        // Recreate FBOs to match new window size
        disposeFrameBuffers();
        createFrameBuffers(width, height);
    }

    @Override
    public void pause() {
        // implement pause
    }

    @Override
    public void resume() {
        // implement resume
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        player = new Player(map, 16, 8);

        stage.addActor(player);
        stage.setKeyboardFocus(player);

        stage.addListener(new KeyHandler(player, this, game));

        stage.getCamera().position.set(16 + player.getWidth() / 2, 8 + player.getHeight() / 2, 0);
        stage.getCamera().update();
    }

    @Override
    public void hide() {
        // implement hide
    }

    @Override
    public void dispose() {
        stage.dispose();
        disposeFrameBuffers();
        collisionFbo.dispose();
        grayScaleShader.dispose();
        fogShader.dispose();
        collisionShader.dispose();
        combinationShader.dispose();
        map.dispose();
        mapRenderer.dispose();
    }

    private void renderWorld(Batch batch, OrthographicCamera camera, float viewX, float viewY, float viewW, float viewH) {
        fbo.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            mapRenderer.setView(camera);
            mapRenderer.render();
            stage.draw();
        fbo.end();

        TextureRegion sceneRegion = fboRegion;
        if (noireMode) {
            noireFbo.begin();
                ScreenUtils.clear(0, 0, 0, 1);
                batch.setProjectionMatrix(camera.combined);
                batch.setShader(grayScaleShader);
                batch.begin();
                    batch.draw(fboRegion, viewX, viewY, viewW, viewH);
                batch.end();
            noireFbo.end();
            sceneRegion = noireFboRegion;
        }

        fboFog.begin();
            ScreenUtils.clear(0, 0, 0, 1);
            batch.setProjectionMatrix(camera.combined);
            batch.setShader(fogShader);

            batch.begin();
                fogShader.setUniformf("u_playerWorldPos", player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);
                fogShader.setUniformf("u_camWorldPos", camera.position.x, camera.position.y);
                fogShader.setUniformf("u_worldViewSize", viewW, viewH);
                fogShader.setUniformf("u_radiusWorld", fogIntensity);

                batch.draw(sceneRegion, viewX, viewY, viewW, viewH);
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

                float uvWidth  = viewW / WORLD_WIDTH;
                float uvHeight = viewH / WORLD_HEIGHT;
                float uvStartX = viewX / WORLD_WIDTH;
                float uvStartY = viewY / WORLD_HEIGHT;

                collisionShader.setUniformi("u_mask", 1);
                collisionShader.setUniformf("u_uvOffset", uvStartX, uvStartY);
                collisionShader.setUniformf("u_uvScale", uvWidth, uvHeight);

                batch.draw(sceneRegion, viewX, viewY, viewW, viewH);
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
        batch.end();
        batch.setShader(null);
    }

    private void createFrameBuffers(int width, int height) {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);

        noireFbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        noireFboRegion = new TextureRegion(noireFbo.getColorBufferTexture());
        noireFboRegion.flip(false, true);

        fboFog = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

        interFbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        interFboRegion = new TextureRegion(interFbo.getColorBufferTexture());
        interFboRegion.flip(false, true);
    }

    private void disposeFrameBuffers() {
        fbo.dispose();
        noireFbo.dispose();
        fboFog.dispose();
        interFbo.dispose();
    }

    private void generateFBO() {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("Walls");
        int height = collisionLayer.getHeight();
        int width = collisionLayer.getWidth();
        collisionFbo = new FrameBuffer(Pixmap.Format.RGBA8888, WORLD_WIDTH, WORLD_HEIGHT, false);
        collisionFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        ShapeRenderer collisionSR = new ShapeRenderer();
        collisionFbo.begin();

            collisionSR.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, WORLD_WIDTH, WORLD_HEIGHT));
            collisionSR.begin(ShapeRenderer.ShapeType.Filled);
            collisionSR.setColor(Color.WHITE);
            collisionSR.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            collisionSR.setColor(new Color(0.3f, 0.3f, 0.3f, 1));

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        if (collisionLayer.getCell(j, i) != null) {
                            collisionSR.rect(j, i, 1, 1);
                        }
                    }
                }

            collisionSR.end();
        collisionFbo.end();
        collisionSR.dispose();
    }
}
