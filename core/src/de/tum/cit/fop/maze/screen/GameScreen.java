package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.system.HUD;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.map.MapLoader;
import de.tum.cit.fop.maze.system.*;
import de.tum.cit.fop.maze.system.ProgressionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Main campaign gameplay screen.
 */
public class GameScreen implements Screen {

    /** World width in tiles. */
    public static final int WORLD_WIDTH = 225;
    /** World height in tiles. */
    public static final int WORLD_HEIGHT = 250;
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Loaded tiled map. */
    private final TiledMap map;
    /** Collision layer for movement. */
    private final TiledMapTileLayer  collisionLayer;
    /** Road layer for BMW movement. */
    private final TiledMapTileLayer roadLayer;
    /** Map renderer for tiled map. */
    private final OrthogonalTiledMapRenderer mapRenderer;
    /** Stage for world actors. */
    private final Stage stage;
    /** Grayscale shader for effects. */
    private final ShaderProgram grayScaleShader;
    /** Combined shader for fog and effects. */
    private final ShaderProgram combinedShader;
    /** UI camera for HUD overlay. */
    private final OrthographicCamera uiCamera;
    /** Particle system for drifting. */
    private final DriftParticleSystem driftParticleSystem;
    /** Fog radius intensity. */
    private float fogIntensity = 5f ;
    /** Whether glasses upgrade has been applied. */
    private boolean glassesApplied = false;
    /** Whether noire mode is enabled. */
    private boolean noireMode = false;
    /** Framebuffer for world rendering. */
    private FrameBuffer fbo;
    /** Region of the framebuffer texture. */
    private TextureRegion fboRegion;
    /** Player actor. */
    private final Player player;
    /** Point manager for scoring. */
    public PointManager pointManager;
    /** Map loader for generating layers and entities. */
    private final MapLoader mapLoader = new MapLoader();
    /** Base map path. */
    private String mapPath = "Assets_Map/THE_MAP.tmx";
    /** Current level number. */
    private int level;
    /** Properties file path for the level. */
    private String propertiesPath;
    /** HUD overlay. */
    private final HUD hud;
    /** Developer console overlay. */
    private final DevConsole devConsole;
    /** Active enemies. */
    private final List<de.tum.cit.fop.maze.entity.obstacle.Enemy> enemies = new ArrayList<>();
    /** Active collectibles. */
    private final List<de.tum.cit.fop.maze.entity.collectible.Collectible> collectibles = new ArrayList<>();
    /** Current saved game state. */
    private GameState gameState;
    /** Whether the game is paused. */
    private boolean paused = false;
    /** Regen interval seconds. */
    private static final float REGEN_INTERVAL_SECONDS = 10f;
    /** Points awarded when regen triggers at full health. */
    private static final int REGEN_POINTS_ON_FULL = 100;
    /** Regen timer accumulator. */
    private float regenTimer = 0f;
    /** Minimum camera zoom. */
    private static final float MIN_ZOOM = 0.05f;
    /** Maximum camera zoom. */
    private static final float MAX_ZOOM = 0.3f;
    /** Fog intensity for early levels. */
    private static final float EARLY_LEVEL_FOG_INTENSITY = 50f;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this(game, 1);
    }

    /**
     * Creates a game screen for a specific level.
     *
     * @param game game instance
     * @param level level number
     */
    public GameScreen(MazeRunnerGame game, int level) {
        this.game = game;
        this.hud = new HUD(game);
        this.devConsole = new DevConsole(game);
        this.level = level;
        this.propertiesPath = toPropertiesPath(level);
        var graphicsManager = game.getGraphicsManager();
        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        String generatedMapPath = buildGeneratedTmx(mapPath, propertiesPath, level);
        map = new TmxMapLoader().load(String.valueOf(Gdx.files.local(generatedMapPath)));
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, graphicsManager.getWidth(), graphicsManager.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));
        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));
        ((OrthographicCamera) stage.getCamera()).zoom = MIN_ZOOM;
        uiCamera = new OrthographicCamera(graphicsManager.getWidth(), graphicsManager.getHeight());
        pointManager = new PointManager(level);
        collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, this.propertiesPath);
        roadLayer = mapLoader.buildRoadLayerFromProperties(map, this.propertiesPath);
        de.tum.cit.fop.maze.entity.obstacle.BmwEnemy.setRoadLayer(roadLayer);
        GridPoint2 spawnPoint = mapLoader.findPlayerSpawnFromProperties(this.propertiesPath);
        float spawnX = spawnPoint != null ? spawnPoint.x : 78f; // 78 x 46 is the default spawn if there is no spawn place in the map
        float spawnY = spawnPoint != null ? spawnPoint.y : 46f;
        player = new Player(collisionLayer, spawnX, spawnY, game::goToGameOverScreen);
        player.setDeathCauseListener(game::handlePlayerDeath);
        player.setWorldBounds(WORLD_WIDTH, WORLD_HEIGHT);
        applyUpgrades();
        applyEarlyLevelFog();
        this.driftParticleSystem = new DriftParticleSystem(player, game.getAudioManager());
        stage.addActor(driftParticleSystem);
        devConsole.setPlayer(player);
        devConsole.setSpawnLayers(collisionLayer, roadLayer);
        devConsole.addToStage(hud.getStage());

    }

    /**
     * Creates a game screen from a saved game state.
     *
     * @param game game instance
     * @param gameState saved state to load
     */
    public GameScreen(MazeRunnerGame game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;
        this.hud = new HUD(game);
        this.devConsole = new DevConsole(game);
        if (gameState.getMapPath() != null) {
            this.mapPath = gameState.getMapPath();
        }
        this.level = gameState.getLevel();
        this.propertiesPath = toPropertiesPath(this.level);
        String generatedMapPath = buildGeneratedTmx(this.mapPath, this.propertiesPath, this.level);
        this.map = new TmxMapLoader().load(String.valueOf(Gdx.files.local(generatedMapPath)));

        var graphicsManager = game.getGraphicsManager();
        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, graphicsManager.getWidth(), graphicsManager.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));
        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));
        ((OrthographicCamera) stage.getCamera()).zoom = MAX_ZOOM;
        uiCamera = new OrthographicCamera(graphicsManager.getWidth(), graphicsManager.getHeight());
        collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, this.propertiesPath);
        roadLayer = mapLoader.buildRoadLayerFromProperties(map, this.propertiesPath);
        de.tum.cit.fop.maze.entity.obstacle.BmwEnemy.setRoadLayer(roadLayer);
        GridPoint2 spawnPoint = mapLoader.findPlayerSpawnFromProperties(this.propertiesPath);
        float spawnX = spawnPoint != null ? spawnPoint.x : 78f;
        float spawnY = spawnPoint != null ? spawnPoint.y : 46f;
        player = new Player(collisionLayer, spawnX, spawnY, game::goToGameOverScreen);
        player.setDeathCauseListener(game::handlePlayerDeath);
        player.setWorldBounds(WORLD_WIDTH, WORLD_HEIGHT);
        applyUpgrades();
        applyEarlyLevelFog();
        devConsole.setPlayer(player);
        devConsole.setSpawnLayers(collisionLayer, roadLayer);
        devConsole.addToStage(hud.getStage());

        this.player.setX(gameState.getPlayerX());
        this.player.setY(gameState.getPlayerY());
        if (gameState.hasKey()) player.pickupKey();
        if (gameState.getPointManager() != null) {
            pointManager = gameState.getPointManager();
        } else {
            pointManager = new PointManager(this.level);
        }
        this.player.setHp(gameState.getPlayerLives());
        this.driftParticleSystem = new DriftParticleSystem(player, game.getAudioManager());
        stage.addActor(driftParticleSystem);
        ((OrthographicCamera) stage.getCamera()).zoom = MathUtils.clamp(gameState.getCameraZoom(), MIN_ZOOM, MAX_ZOOM);
    }

    /**
     * Adjusts camera zoom by the given amount.
     *
     * @param amount zoom delta
     */
    public void adjustZoom(float amount) {
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();
        camera.zoom = MathUtils.clamp(camera.zoom + amount, MIN_ZOOM, MAX_ZOOM);
        camera.update();
    }

    /**
     * Generates a TMX file for the level based on properties.
     *
     * @param templateMapPath TMX template path
     * @param propertiesPath properties path for tiles
     * @param level level number
     * @return output TMX path
     */
    private String buildGeneratedTmx(String templateMapPath, String propertiesPath, int level) {
        String outputPath = String.format("assets/Assets_Map/generated-level-%d.tmx", level);
        mapLoader.buildTmxFromProperties(propertiesPath, templateMapPath, outputPath);
        return outputPath;
    }

    /**
     * Adjusts fog intensity by an amount.
     *
     * @param amount delta to apply
     */
    public void adjustFog(float amount) {
        fogIntensity += amount;
    }

    /**
     * Toggles noire mode.
     */
    public void toggleNoireMode() {
        noireMode = !noireMode;
    }

    /**
     * Increases fog intensity for early levels.
     */
    private void applyEarlyLevelFog() {
        if (level == 1 || level == 2) {
            fogIntensity = Math.max(fogIntensity, EARLY_LEVEL_FOG_INTENSITY);
        }
    }

    // If I have more time ill make it, so this isn't polled every frame (the original idea was to make it fired in the upgrade classes),
    // But for now this also works
    /**
     * Applies upgrade effects to the player each frame.
     */
    private void applyUpgrades() {
        ProgressionManager progressionManager = game.getProgressionManager();
        int speedUpgrades = 0;
        if (progressionManager.hasUpgrade("speed")) speedUpgrades++;
        if (progressionManager.hasUpgrade("speed_2")) speedUpgrades++;
        if (progressionManager.hasUpgrade("speed_3")) speedUpgrades++;
        float multiplier = 1f + (0.2f * speedUpgrades);
        player.setSpeedMultiplier(multiplier);

        int healthUpgrades = 0;
        if (progressionManager.hasUpgrade("health")) healthUpgrades++;
        if (progressionManager.hasUpgrade("health_2")) healthUpgrades++;
        if (progressionManager.hasUpgrade("health_3")) healthUpgrades++;
        player.setMaxHp(3 + healthUpgrades);

        int drinkSpeedUpgrades = 0;
        if (progressionManager.hasUpgrade("drink_speed_1")) drinkSpeedUpgrades++;
        if (progressionManager.hasUpgrade("drink_speed_2")) drinkSpeedUpgrades++;
        float drinkMultiplier = 1f + (0.5f * drinkSpeedUpgrades);
        player.setDrinkDurationMultiplier(drinkMultiplier);

        player.setPotholeImmune(progressionManager.hasUpgrade("pothol_imunity"));
        player.setDetectionRangeMultiplier(progressionManager.hasUpgrade("stealth") ? 0.75f : 1f);

        if (progressionManager.hasUpgrade("new_glasses") && !glassesApplied) {
            fogIntensity += 10f;
            glassesApplied = true;
        }
    }

    /**
     * Renders and updates the game.
     *
     * @param delta frame delta time
     */
    @Override
    public void render(float delta) {
        hud.setShopButtonVisible(false);

        if (!paused) {
            applyUpgrades();
            handleRegen(delta);
            stage.act(delta);
            pointManager.act(delta);
        }
        // Doing it through a listener is better, as this happens every frame, but this is easier
        noireMode = player.getHp() <= 1;

        Batch batch = stage.getBatch();
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();

        float halfViewW = (camera.viewportWidth * camera.zoom) / 2f;
        float halfViewH = (camera.viewportHeight * camera.zoom) / 2f;
        float maxX = WORLD_WIDTH - halfViewW;
        float maxY = WORLD_HEIGHT - halfViewH;
        float minX = Math.min(halfViewW, maxX);
        float maxClampX = Math.max(halfViewW, maxX);
        float minY = Math.min(halfViewH, maxY);
        float maxClampY = Math.max(halfViewH, maxY);
        camera.position.x = MathUtils.clamp(camera.position.x, minX, maxClampX);
        camera.position.y = MathUtils.clamp(camera.position.y, minY, maxClampY);
        camera.update();

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
        float keyX = Float.NaN;
        float keyY = Float.NaN;
        float exitX = Float.NaN;
        float exitY = Float.NaN;
        for (de.tum.cit.fop.maze.entity.collectible.Collectible collectible : collectibles) {
            if (collectible instanceof de.tum.cit.fop.maze.entity.collectible.Key && !collectible.getPickedUp()) {
                keyX = collectible.getSpawnX();
                keyY = collectible.getSpawnY();
            } else if (collectible instanceof de.tum.cit.fop.maze.entity.collectible.ExitDoor) {
                exitX = collectible.getSpawnX();
                exitY = collectible.getSpawnY();
            }
        }
        hud.update(
                level,
                player.getHp(),
                pointManager.getPoints(),
                player.hasKey(),
                game.getProgressionManager().hasUpgrade("regen"),
                regenTimer,
                REGEN_INTERVAL_SECONDS,
                -1f,
                player.getX() + player.getWidth() / 2f,
                player.getY() + player.getHeight() / 2f,
                keyX, keyY, exitX, exitY
        );
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
            gameState.save(
                    mapPath,
                    level,
                    ((OrthographicCamera) stage.getCamera()).zoom,
                    player.getX(),
                    player.getY(),
                    player.getHp(),
                    pointManager,
                    player.hasKey(),
                    enemyDataList,
                    collectibleDataList,
                    game.getProgressionManager().getPoints(),
                    new java.util.HashSet<>(game.getProgressionManager().getOwnedUpgrades())
            );
        }
        else {
            gameState = new GameState(
                    mapPath,
                    level,
                    ((OrthographicCamera) stage.getCamera()).zoom,
                    player.getX(),
                    player.getY(),
                    player.getHp(),
                    pointManager,
                    player.hasKey(),
                    enemyDataList,
                    collectibleDataList,
                    game.getProgressionManager().getPoints(),
                    new java.util.HashSet<>(game.getProgressionManager().getOwnedUpgrades())
            );
        }
        SaveManager.saveGame(gameState);
    }

    /**
     * Resizes viewports and framebuffers.
     *
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
        hud.resize(width, height);

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        // Recreate FBOs to match the new window size
        fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
    }

    /**
     * Toggles pause state and pause menu visibility.
     */
    @Override
    public void pause() {
        if (paused) {
            resume();
        } else {
            paused = true;
            hud.setPauseMenuVisible(true);
        }
    }

    /**
     * Resumes gameplay and hides pause menu.
     */
    @Override
    public void resume() {
        paused = false;
        hud.setPauseMenuVisible(false);
    }

    /**
     * Sets input processing and initializes actors on show.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(hud.getStage(), stage));
        stage.addActor(player);

        if (enemies.isEmpty() && collectibles.isEmpty()) {
            mapLoader.spawnEntitiesFromProperties(stage, pointManager, collisionLayer, roadLayer, propertiesPath, hud, enemies, collectibles, game::goToVictoryScreen);
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

        stage.getCamera().position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
        stage.getCamera().update();
    }

    @Override
    public void hide() {
    }

    /**
     * Disposes all resources associated with the screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        hud.dispose();
        fbo.dispose();
        grayScaleShader.dispose();
        combinedShader.dispose();
        map.dispose();
        mapRenderer.dispose();
    }

    /**
     * Returns whether the game is paused.
     *
     * @return {@code true} if paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns the HUD instance.
     *
     * @return HUD
     */
    public HUD getHud() {
        return hud;
    }

    /**
     * Toggles the developer console.
     */
    public void toggleDevConsole() {
        devConsole.toggle(hud.getStage());
    }

    /**
     * Returns whether the developer console is visible.
     *
     * @return {@code true} if visible
     */
    public boolean isDevConsoleVisible() {
        return devConsole.isVisible();
    }

    /**
     * Builds a properties path for a level.
     *
     * @param levelNumber level number
     * @return properties file path
     */
    private static String toPropertiesPath(int levelNumber) {
        return String.format("maps/level-%d.properties", levelNumber);
    }

    /**
     * Applies regeneration over time if the upgrade is owned.
     *
     * @param delta frame delta time
     */
    private void handleRegen(float delta) {
        if (!game.getProgressionManager().hasUpgrade("regen")) {
            regenTimer = 0f;
            return;
        }
        regenTimer += delta;

        if (regenTimer >= REGEN_INTERVAL_SECONDS) {
            regenTimer = 0;
            if (player.getHp() < player.getMaxHp()) {
                player.setHp(player.getHp() + 1);
            } else {
                pointManager.add(REGEN_POINTS_ON_FULL);
            }
        }
    }
}
