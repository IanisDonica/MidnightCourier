package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.HUD;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.map.MapLoader;
import de.tum.cit.fop.maze.system.KeyHandler;
import de.tum.cit.fop.maze.system.PointManager;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    public static final int WORLD_WIDTH = 32;
    public static final int WORLD_HEIGHT = 16;
    private final MazeRunnerGame game;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;
    private final ShaderProgram grayScaleShader;
    private final ShaderProgram combinedShader;
    private final OrthographicCamera uiCamera;
    private float fogIntensity = 20f;
    private boolean noireMode = false;
    private FrameBuffer fbo;
    private TextureRegion fboRegion;
    private Player player;
    public PointManager pointManager;
    private final MapLoader mapLoader = new MapLoader();
    private final String propertiesPath = "maps/level-1.properties";
    private HUD hud;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        this.hud = new HUD(game);
        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        map = new TmxMapLoader().load("untitled.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);

        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));

        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));

        ((OrthographicCamera) stage.getCamera()).zoom = 1f;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        pointManager = new PointManager();
    }

    public void adjustZoom(float amount) {
        ((OrthographicCamera) stage.getCamera()).zoom += amount;
    }

    public void adjustFog(float amount) {
        fogIntensity += amount;
    }

    public void toggleNoireMode() {
        noireMode = !noireMode;
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        pointManager.act(delta);
        //System.out.println(pointManager.getPoints());

        Batch batch = stage.getBatch();
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();

        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float viewX = camera.position.x - viewW / 2f;
        float viewY = camera.position.y - viewH / 2f;

        // 1. Render map and stage to FBO
        fbo.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        mapRenderer.setView(camera);
        mapRenderer.render();
        stage.draw();
        fbo.end();

        // 2. Render to screen with combined shader (fog)
        stage.getViewport().apply();
        ScreenUtils.clear(0, 0, 0, 1);
        batch.setProjectionMatrix(camera.combined);
        batch.setShader(combinedShader);
        batch.begin();
            // Set uniforms for fog
            combinedShader.setUniformf("u_playerWorldPos", player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);
            combinedShader.setUniformf("u_camWorldPos", camera.position.x, camera.position.y);
            combinedShader.setUniformf("u_worldViewSize", viewW, viewH);
            combinedShader.setUniformf("u_radiusWorld", fogIntensity);

            combinedShader.setUniformi("u_noireMode", noireMode ? 1 : 0);

            // Draw the base world FBO
            batch.draw(fboRegion, viewX, viewY, viewW, viewH);
        batch.end();
        batch.setShader(null);

        // render hud
        hud.update(player.getHp(), pointManager.getPoints(), player.hasKey());
        hud.getStage().act(delta);
        hud.getStage().draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        // Recreate FBOs to match the new window size
        fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
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

        TiledMapTileLayer collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, propertiesPath);
        player = new Player(collisionLayer, 16, 8);
        stage.addActor(player);

        mapLoader.spawnCollectiblesFromProperties(stage, pointManager, collisionLayer, propertiesPath);

        stage.setKeyboardFocus(player);
        player.toFront();
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
        fbo.dispose();
        grayScaleShader.dispose();
        combinedShader.dispose();
        map.dispose();
        mapRenderer.dispose();
    }
}
