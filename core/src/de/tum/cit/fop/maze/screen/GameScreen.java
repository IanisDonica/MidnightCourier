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
import de.tum.cit.fop.maze.system.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    public static final int WORLD_WIDTH = 225;
    public static final int WORLD_HEIGHT = 250;
    private final MazeRunnerGame game;
    private final TiledMap map;
    private final TiledMapTileLayer  collisionLayer;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;
    private final ShaderProgram grayScaleShader;
    private final ShaderProgram combinedShader;
    private final OrthographicCamera uiCamera;
    private float fogIntensity = 20f;
    private boolean noireMode = false;
    private FrameBuffer fbo;
    private TextureRegion fboRegion;
    private final Player player;
    public PointManager pointManager;
    private final MapLoader mapLoader = new MapLoader();
    private String mapPath = "Assets_Map/THE_MAP.tmx";
    private int level;
    private String propertiesPath;
    private final HUD hud;
    private final List<de.tum.cit.fop.maze.entity.obstacle.Enemy> enemies = new ArrayList<>();
    private final List<de.tum.cit.fop.maze.entity.collectible.Collectible> collectibles = new ArrayList<>();
    private GameState gameState;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this(game, 1);
    }

    public GameScreen(MazeRunnerGame game, int level) {
        this.game = game;
        this.hud = new HUD(game);
        this.level = level;
        this.propertiesPath = toPropertiesPath(level);
        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));
        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));
        ((OrthographicCamera) stage.getCamera()).zoom = 0.1f;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        pointManager = new PointManager(level);
        collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, this.propertiesPath);
        player = new Player(collisionLayer, 16, 10, game::goToGameOverScreen);

    }

    public GameScreen(MazeRunnerGame game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;
        this.hud = new HUD(game);
        if (gameState.getMapPath() != null) {
            this.mapPath = gameState.getMapPath();
        }
        this.level = gameState.getLevel();
        this.propertiesPath = toPropertiesPath(this.level);
        this.map = new TmxMapLoader().load(this.mapPath);

        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));
        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));
        ((OrthographicCamera) stage.getCamera()).zoom = 1f;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, this.propertiesPath);
        player = new Player(collisionLayer, 16, 10, game::goToGameOverScreen);

        this.player.setX(gameState.getPlayerX());
        this.player.setY(gameState.getPlayerY());
        if (gameState.hasKey()) player.pickupKey();
        if (gameState.getPointManager() != null) {
            pointManager = gameState.getPointManager();
        } else {
            pointManager = new PointManager(this.level);
        }
        this.player.setHp(gameState.getPlayerLives());
        ((OrthographicCamera) stage.getCamera()).zoom = gameState.getCameraZoom();
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
        // Doing it through a listener is better, as this happens every frame, but this is easier
        noireMode = player.getHp() <= 1;

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
        hud.update(level, player.getHp(), pointManager.getPoints(), player.hasKey());
        hud.getStage().act(delta);
        hud.getStage().draw();

        List<EnemyData> enemyDataList = new ArrayList<>();
        List<CollectibleData> collectibleDataList = new ArrayList<>();

        for (de.tum.cit.fop.maze.entity.obstacle.Enemy enemy : enemies) {
            enemyDataList.add(new EnemyData(enemy.getX(), enemy.getY()));
        }
        for (de.tum.cit.fop.maze.entity.collectible.Collectible collectible : collectibles) {
            // We use the initial coordinates as ID.
            collectibleDataList.add(new CollectibleData(collectible.getSpawnX(), collectible.getSpawnY(), collectible.getPickedUp()));
        }

        if (gameState != null) {
            gameState.save(mapPath, level, ((OrthographicCamera) stage.getCamera()).zoom, player.getX(), player.getY(), player.getHp(), pointManager, player.hasKey(), enemyDataList, collectibleDataList);
        }
        else gameState = new GameState(mapPath, level, ((OrthographicCamera) stage.getCamera()).zoom, player.getX(), player.getY(), player.getHp(), pointManager, player.hasKey(), enemyDataList, collectibleDataList);
        SaveManager.saveGame(gameState);
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

    }

    @Override
    public void resume() {
        // implement resumeAS
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addActor(player);

        if (enemies.isEmpty() && collectibles.isEmpty()) {
            mapLoader.spawnEntitiesFromProperties(stage, pointManager, collisionLayer, propertiesPath, enemies, collectibles, game::goToVictoryScreen);
        }

        if (gameState != null) {
            if (gameState.getEnemies() != null) {
                for (int i = 0; i < Math.min(enemies.size(), gameState.getEnemies().size()); i++) {
                    EnemyData data = gameState.getEnemies().get(i);
                    enemies.get(i).setPosition(data.x, data.y);
                }
            }

            if (gameState.getCollectibles() != null) {
                for (CollectibleData data : gameState.getCollectibles()) {
                    if (data.pickedUp) {
                        for (de.tum.cit.fop.maze.entity.collectible.Collectible collectible : collectibles) {
                            if (Math.abs(collectible.getSpawnX() - data.x) < 0.01f && Math.abs(collectible.getSpawnY() - data.y) < 0.01f) {
                                collectible.markPickedUp();
                                collectible.remove();
                            }
                        }
                    }
                }
            }
        }

        stage.setKeyboardFocus(player);
        player.toFront();
        stage.addListener(game.getKeyHandler());
        game.getKeyHandler().setPlayer(player);
        game.getKeyHandler().setGameScreen(this);

        stage.getCamera().position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
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

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        if (player != null) {
            this.level = gameState.getLevel();
            this.propertiesPath = toPropertiesPath(this.level);
            player.setPosition(gameState.getPlayerX(), gameState.getPlayerY());
            // Since there's no dropKey, we just set the state if it's true.
            // If it's false, we'd need a way to unset it if we want to support full state restoration.
            if (gameState.hasKey()) player.pickupKey();
            player.setHp(gameState.getPlayerLives());
            ((OrthographicCamera) stage.getCamera()).zoom = gameState.getCameraZoom();
        }
        this.pointManager = gameState.getPointManager();
    }

    private static String toPropertiesPath(int levelNumber) {
        return String.format("maps/level-%d.properties", levelNumber);
    }
}
