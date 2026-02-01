package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.entity.obstacle.BmwEnemy;
import de.tum.cit.fop.maze.entity.obstacle.Enemy;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.map.MapLoader;
import de.tum.cit.fop.maze.system.UiUtils;

/**
 * Main menu screen with animated background.
 */
public class MenuScreen implements Screen {
    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Audio manager for UI sounds. */
    private final AudioManager audioManager;
    /** Vignette texture overlay. */
    private final Texture vignetteTexture;
    /** Background map used for menu animation. */
    private final TiledMap backgroundMap;
    /** Renderer for the background map. */
    private final OrthogonalTiledMapRenderer backgroundRenderer;
    /** Camera used for the background map. */
    private final OrthographicCamera backgroundCamera;
    /** Map loader for background assets. */
    private final MapLoader mapLoader;
    /** Stage for background actors. */
    private final Stage backgroundStage;
    /** Stage for vignette overlay. */
    private final Stage vignetteStage;
    /** Collision layer for background actors. */
    private final TiledMapTileLayer collisionLayer;
    /** Road layer for background BMWs. */
    private final TiledMapTileLayer roadLayer;
    /** Background camera viewport height. */
    private static final float BACKGROUND_VIEW_HEIGHT = 100f;
    /** Background camera X offset. */
    private static final float BACKGROUND_OFFSET_X = -100f;
    /** Background camera Y offset. */
    private static final float BACKGROUND_OFFSET_Y = 0f;

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        camera.zoom = 1f; // UI camera zoom
        audioManager = game.getAudioManager();
        var graphicsManager = game.getGraphicsManager();

        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight(), camera);
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        mapLoader = new MapLoader();
        String propertiesPath = "maps/level-6.properties";
        String templateMapPath = "Assets_Map/THE_MAP.tmx";
        String outputPath = "assets/Assets_Map/generated-menu-level-6.tmx";
        mapLoader.buildTmxFromProperties(propertiesPath, templateMapPath, outputPath);
        backgroundMap = new TmxMapLoader().load(String.valueOf(Gdx.files.local(outputPath)));
        backgroundRenderer = new OrthogonalTiledMapRenderer(backgroundMap, 1 / 32f, game.getSpriteBatch());
        backgroundCamera = new OrthographicCamera();
        updateBackgroundCamera(graphicsManager.getWidth(), graphicsManager.getHeight());
        centerBackgroundCamera();
        backgroundStage = new Stage(new FitViewport(backgroundCamera.viewportWidth, backgroundCamera.viewportHeight, backgroundCamera), game.getSpriteBatch());
        collisionLayer = mapLoader.buildCollisionLayerFromProperties(backgroundMap, propertiesPath);
        roadLayer = mapLoader.buildRoadLayerFromProperties(backgroundMap, propertiesPath);
        BmwEnemy.setRoadLayer(roadLayer);
        MenuDummyPlayer dummyPlayer = new MenuDummyPlayer(collisionLayer, 0f, 0f);
        backgroundStage.addActor(dummyPlayer);
        centerDummyPlayer(dummyPlayer);
        Enemy.spawnRandomEnemies(dummyPlayer, backgroundStage, collisionLayer, 400);
        BmwEnemy.spawnRandomBmws(dummyPlayer, backgroundStage, 600);

        vignetteTexture = UiUtils.buildVignetteTexture(512, 512, 0.9f);
        Image vignetteImage = new Image(vignetteTexture);
        vignetteImage.setFillParent(true);
        vignetteImage.setTouchable(Touchable.disabled);
        vignetteStage = new Stage(new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight()), game.getSpriteBatch());
        vignetteStage.addActor(vignetteImage);

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Midnight Courier", game.getSkin(), "title")).padBottom(80).row();

        // Create and add a button to go to the game screen

        TextButton newGame = new TextButton("New Game", game.getSkin());
        table.add(newGame).width(500).padBottom(15).row();

        TextButton continueGame = new TextButton("Load Game", game.getSkin());
        table.add(continueGame).width(500).padBottom(15).row();

        TextButton loadSpecificLevel = new TextButton("Load specific level", game.getSkin());
        table.add(loadSpecificLevel).width(500).padBottom(15).row();

        TextButton highscores = new TextButton("Highscores", game.getSkin());
        table.add(highscores).width(500).padBottom(15).row();

        TextButton achievements = new TextButton("Achievements", game.getSkin());
        table.add(achievements).width(500).padBottom(15).row();

        TextButton settings = new TextButton("Settings", game.getSkin());
        table.add(settings).width(500).padBottom(15).row();

        TextButton exit = new TextButton("Exit", game.getSkin());
        table.add(exit).width(500).padBottom(15).row();

        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToNewGameScreen(); // Change to the game screen when the button is pressed
            }
        });

        continueGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToContinueGameScreen(); // Change to the game screen when the button is pressed
            }
        });

        loadSpecificLevel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToLevelSelectScreen();
            }
        });

        highscores.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToHighscoreScreen();
            }
        });

        achievements.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToAchievementsScreen();
            }
        });

        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goToSettingsScreen(); // Change to the game screen when the button is pressed
            }
        });

        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                Gdx.app.exit();
            }
        });
    }

    /**
     * Renders the menu and its animated background.
     *
     * @param delta frame delta time
     */
    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        }
        renderBackground(delta);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    /**
     * Updates viewports and background camera on resize.
     *
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
        updateBackgroundCamera(game.getGraphicsManager().getWidth(), game.getGraphicsManager().getHeight());
        centerBackgroundCamera();
        backgroundStage.getViewport().update(game.getGraphicsManager().getWidth(), game.getGraphicsManager().getHeight(), false);
        vignetteStage.getViewport().update(game.getGraphicsManager().getWidth(), game.getGraphicsManager().getHeight(), true);
    }

    /**
     * Disposes menu resources.
     */
    @Override
    public void dispose() {
        // Dispose of the stage when the screen is disposed
        stage.dispose();
        backgroundStage.dispose();
        vignetteStage.dispose();
        vignetteTexture.dispose();
        backgroundRenderer.dispose();
        backgroundMap.dispose();
    }

    /**
     * Sets input processing for the menu.
     */
    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    /**
     * Creates a solid color texture.
     *
     * @param w width in pixels
     * @param h height in pixels
     * @param color texture color
     * @return created texture
     */
    private Texture makeSolidTexture(int w, int h, Color color) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    /**
     * Updates the background camera viewport to match the window aspect.
     *
     * @param width window width
     * @param height window height
     */
    private void updateBackgroundCamera(int width, int height) {
        float aspect = width / (float) height;
        backgroundCamera.viewportHeight = BACKGROUND_VIEW_HEIGHT;
        backgroundCamera.viewportWidth = BACKGROUND_VIEW_HEIGHT * aspect;
        backgroundCamera.update();
    }

    /**
     * Renders the animated background and vignette overlay.
     *
     * @param delta frame delta time
     */
    public void renderBackground(float delta) {
        var graphicsManager = game.getGraphicsManager();
        backgroundStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        backgroundStage.getViewport().apply();
        updateBackgroundCamera(graphicsManager.getWidth(), graphicsManager.getHeight());
        centerBackgroundCamera();
        backgroundRenderer.setView(backgroundCamera);
        backgroundRenderer.render();
        backgroundStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        backgroundStage.draw();
        vignetteStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        vignetteStage.draw();
    }

    /**
     * Centers the background camera on the map, respecting bounds.
     */
    private void centerBackgroundCamera() {
        TiledMapTileLayer layer = (TiledMapTileLayer) backgroundMap.getLayers().get(0);
        float mapCenterX = layer.getWidth() / 2f + BACKGROUND_OFFSET_X;
        float mapCenterY = layer.getHeight() / 2f + BACKGROUND_OFFSET_Y;
        float halfW = backgroundCamera.viewportWidth / 2f;
        float halfH = backgroundCamera.viewportHeight / 2f;
        float minX = halfW;
        float maxX = layer.getWidth() - halfW;
        float minY = halfH;
        float maxY = layer.getHeight() - halfH;
        mapCenterX = MathUtils.clamp(mapCenterX, minX, maxX);
        mapCenterY = MathUtils.clamp(mapCenterY, minY, maxY);
        backgroundCamera.position.set(mapCenterX, mapCenterY, 0f);
        backgroundCamera.update();
    }

    /**
     * Centers the dummy player on the background map.
     *
     * @param dummyPlayer dummy player actor
     */
    private void centerDummyPlayer(MenuDummyPlayer dummyPlayer) {
        TiledMapTileLayer layer = (TiledMapTileLayer) backgroundMap.getLayers().get(0);
        float mapCenterX = layer.getWidth() / 2f + BACKGROUND_OFFSET_X;
        float mapCenterY = layer.getHeight() / 2f + BACKGROUND_OFFSET_Y;
        float halfW = backgroundCamera.viewportWidth / 2f;
        float halfH = backgroundCamera.viewportHeight / 2f;
        float minX = halfW;
        float maxX = layer.getWidth() - halfW;
        float minY = halfH;
        float maxY = layer.getHeight() - halfH;
        mapCenterX = MathUtils.clamp(mapCenterX, minX, maxX);
        mapCenterY = MathUtils.clamp(mapCenterY, minY, maxY);
        dummyPlayer.setPosition(mapCenterX, mapCenterY);
    }

    /**
     * Invisible player used for spawning background enemies.
     */
    private static class MenuDummyPlayer extends Player {
        /**
         * Creates a dummy player for background animations.
         *
         * @param collisionLayer collision layer for movement checks
         * @param x start x
         * @param y start y
         */
        public MenuDummyPlayer(TiledMapTileLayer collisionLayer, float x, float y) {
            super(collisionLayer, x, y, null);
            setVisible(false);
            setSize(0f, 0f);
        }

        @Override
        public void act(float delta) {
            // No movement or camera updates in menu background.
        }
    }
}
