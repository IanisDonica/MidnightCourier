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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.system.HUD;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.map.MapLoader;
import de.tum.cit.fop.maze.system.*;
import de.tum.cit.fop.maze.system.ProgressionManager;
import de.tum.cit.fop.maze.entity.obstacle.Enemy;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import de.tum.cit.fop.maze.entity.obstacle.BmwEnemy;

public class SurvivalScreen implements Screen {
    public static final int WORLD_WIDTH = 225;
    public static final int WORLD_HEIGHT = 250;
    private final MazeRunnerGame game;
    private final TiledMap map;
    private final TiledMapTileLayer  collisionLayer;
    private final TiledMapTileLayer roadLayer;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Stage stage;
    private final ShaderProgram grayScaleShader;
    private final ShaderProgram combinedShader;
    private final OrthographicCamera uiCamera;
    private float fogIntensity = 7f;
    private boolean glassesApplied = false;
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
    private final DevConsole devConsole;
    private final List<de.tum.cit.fop.maze.entity.obstacle.Enemy> enemies = new ArrayList<>();
    private final List<de.tum.cit.fop.maze.entity.collectible.Collectible> collectibles = new ArrayList<>();
    private GameState gameState;
    private boolean paused = false;
    private static final float REGEN_INTERVAL_SECONDS = 10f;
    private static final int REGEN_POINTS_ON_FULL = 100;
    private float regenTimer = 0f;
    private static final float MIN_ZOOM = 0.03f;
    private static final float MAX_ZOOM = 0.3f;
    private int Delta = 0;
    private float Adder = 1;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    ///public SurvivalScreen(MazeRunnerGame game) {
        ///this(game, 1);
    ///}

    public SurvivalScreen(MazeRunnerGame game) {
        this.game = game;
        this.hud = new HUD(game);
        this.devConsole = new DevConsole(game);
        this.level = 0;
        this.propertiesPath = toPropertiesPath(level);
        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        String generatedMapPath = buildGeneratedTmx(mapPath, propertiesPath, level);
        map = new TmxMapLoader().load(String.valueOf(Gdx.files.local(generatedMapPath)));
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);

        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));
        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));
        ((OrthographicCamera) stage.getCamera()).zoom = MIN_ZOOM;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        pointManager = new PointManager(level);

        collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, this.propertiesPath);
        roadLayer = mapLoader.buildRoadLayerFromProperties(map, this.propertiesPath);
        de.tum.cit.fop.maze.entity.obstacle.BmwEnemy.setRoadLayer(roadLayer);

        player = new Player(collisionLayer, 78,46, game::goToGameOverScreen);
        player.setDeathOverListener(game::goToDeathOverScreen);

        player.setWorldBounds(WORLD_WIDTH, WORLD_HEIGHT);
        applyUpgrades();

        devConsole.setPlayer(player);
        devConsole.setSpawnLayers(collisionLayer, roadLayer);
        devConsole.addToStage(hud.getStage());
    }

    public SurvivalScreen(MazeRunnerGame game, GameState gameState) {
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

        Viewport viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        stage = new Stage(viewport, game.getSpriteBatch());
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, game.getSpriteBatch());
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
        grayScaleShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/grayscale.frag"));
        combinedShader = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl"), Gdx.files.internal("shaders/combined.frag"));
        ((OrthographicCamera) stage.getCamera()).zoom = MAX_ZOOM;
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        collisionLayer = mapLoader.buildCollisionLayerFromProperties(map, this.propertiesPath);
        roadLayer = mapLoader.buildRoadLayerFromProperties(map, this.propertiesPath);
        BmwEnemy.setRoadLayer(roadLayer);
        player = new Player(collisionLayer, 78,46, game::goToGameOverScreen);
        player.setDeathOverListener(game::goToDeathOverScreen);
        player.setWorldBounds(WORLD_WIDTH, WORLD_HEIGHT);
        applyUpgrades();
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
        ((OrthographicCamera) stage.getCamera()).zoom = MathUtils.clamp(gameState.getCameraZoom(), MIN_ZOOM, MAX_ZOOM);
    }

    public void adjustZoom(float amount) {
        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();
        camera.zoom = MathUtils.clamp(camera.zoom + amount, MIN_ZOOM, MAX_ZOOM);
        camera.update();
    }

    private String buildGeneratedTmx(String templateMapPath, String propertiesPath, int level) {
        String outputPath = String.format("assets/Assets_Map/generated-survival-level-%d.tmx", level);
        mapLoader.buildTmxFromProperties(propertiesPath, templateMapPath, outputPath);
        return outputPath;
    }

    public void adjustFog(float amount) {
        fogIntensity += amount;
    }

    public void toggleNoireMode() {
        noireMode = !noireMode;
    }

    // If I have more time ill make it, so this isn't polled every frame (the original idea was to make it fired in the upgrade classes),
    // But for now this also works
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

        if (progressionManager.hasUpgrade("new_glasses") && !glassesApplied) {
            fogIntensity += 2f;
            glassesApplied = true;
        }
    }

    @Override
    public void render(float delta) {
        Adder *= 1.0003f;
        Adder = MathUtils.clamp(Adder, 10f,  1000f);
        Delta += (int) Adder;
        System.out.println(Adder);


        Random random = new Random();

        while(Delta >= 80)
        {
            System.out.println(Adder);
            Delta = -80;
            Enemy enemy = new Enemy(collisionLayer, player.getX() + random.nextInt(5, 30), player.getY() + random.nextInt(5, 30));
            stage.addActor(enemy);
            enemies.add(enemy);
        }


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

    @Override
    public void pause() {
        if (paused) {
            resume();
        } else {
            paused = true;
            hud.setPauseMenuVisible(true);
        }
    }

    @Override
    public void resume() {
        paused = false;
        hud.setPauseMenuVisible(false);
    }

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
        // implement hide
    }

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

    public boolean isPaused() {
        return paused;
    }

    public HUD getHud() {
        return hud;
    }

    public void toggleDevConsole() {
        devConsole.toggle(hud.getStage());
    }

    public boolean isDevConsoleVisible() {
        return devConsole.isVisible();
    }

    private static String toPropertiesPath(int levelNumber) {
        return String.format("maps/level-%d.properties", levelNumber);
    }

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
