package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.screen.*;
import de.tum.cit.fop.maze.system.*;
import de.tum.cit.fop.maze.screen.AchievementPopupScreen;
import de.tum.cit.fop.maze.entity.DeathCause;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * Core game class that manages screens and shared resources.
 */

public class MazeRunnerGame extends Game {
    /** Manages configuration and key bindings. */
    private final ConfigManager configManager;
    /** Tracks input bindings and key state. */
    private final KeyHandler keyHandler;
    /** Handles audio playback and settings. */
    private final AudioManager audioManager;
    /** Manages loaded graphics resources. */
    private final GraphicsManager graphicsManager;
    /** Overlay popup screen for achievements. */
    private AchievementPopupScreen achievementPopupScreen;
    /** Main menu screen. */
    private MenuScreen menuScreen;
    /** Main game screen. */
    private GameScreen gameScreen;
    /** Endless/survival mode screen. */
    private SurvivalScreen survivalScreen;
    /** Settings root screen. */
    private SettingsScreen settingsScreen;
    /** Level selection screen. */
    private LevelSelectScreen levelSelectScreen;
    /** Highscore display screen. */
    private HighscoreScreen highscoreScreen;
    /** Progression tree screen. */
    private ProgressionTreeScreen progressionTreeScreen;
    /** Current level number for level-based gameplay. */
    private int currentLevelNumber = 1;
    /** Sprite batch shared across screens. */
    private SpriteBatch spriteBatch;
    /** UI skin shared across screens. */
    private Skin skin;
    /** Progression manager tracking upgrades and points. */
    private ProgressionManager progressionManager;
    /** Screen to return to after leaving settings. */
    private Screen settingsReturnScreen;


    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in a desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        configManager = new ConfigManager();
        this.audioManager = new AudioManager(this);
        keyHandler = new KeyHandler(this);
        graphicsManager = new GraphicsManager();
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        configManager.loadKeyBindings();
        audioManager.loadSettings();
        AchievementManager.init();
        spriteBatch = new SpriteBatch(); // Create SpriteBatch

        TextureAtlas mainAtlas = new TextureAtlas(Gdx.files.internal("craft/craftacular-ui.atlas"));
        TextureAtlas extraAtlas = new TextureAtlas(Gdx.files.internal("craft/extra.atlas"));

        skin = new Skin();
        skin.addRegions(mainAtlas);
        skin.addRegions(extraAtlas);

        skin.load(Gdx.files.internal("craft/craftacular-ui.json"));

        ///skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        ///skin.addRegions(new TextureAtlas(Gdx.files.internal("craft/extra.atlas")));
        achievementPopupScreen = new AchievementPopupScreen(this);
        AchievementManager.setPopupScreen(achievementPopupScreen);
        progressionManager = new ProgressionManager(2000);
        audioManager.preloadSounds("Click.wav");
        audioManager.playMusic("background.mp3", 1f, true);
        graphicsManager.load();
        goToMenu(); // Navigate to the menu screen
    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        this.setScreen(menuScreen);
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        if (gameScreen == null) {
            gameScreen = new GameScreen(this, Math.max(currentLevelNumber, 1));
        }
        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        this.setScreen(gameScreen);
    }

    /**
     * Switches to endless mode from the current screen.
     */
    public void goToEndless() {
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        survivalScreen = new SurvivalScreen(this);
        currentLevelNumber = 0;
        this.setScreen(survivalScreen);
    }

    /**
     * Switches to endless mode while tracking a specific level number.
     *
     * @param levelNumber the level number to store for endless mode
     */
    public void goToEndless(int levelNumber) {
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        currentLevelNumber = levelNumber;
        survivalScreen = new SurvivalScreen(this);
        this.setScreen(survivalScreen);
    }

    /**
     * Switches to survival mode.
     */
    public void goToSurvival() {
        if (survivalScreen == null) {
            survivalScreen = new SurvivalScreen(this);
        }
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        this.setScreen(survivalScreen);
    }

    /**
     * Switches to the game screen for a specific level.
     *
     * @param levelNumber the level to load
     */
    public void goToGame(int levelNumber) {
        // This will be the case after Try again/Next level
        // If this is not done, there will be a memory leak
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        currentLevelNumber = levelNumber;
        gameScreen = new GameScreen(this, currentLevelNumber);
        this.setScreen(gameScreen);
    }

    /**
     * Switches to the game screen from a saved game state.
     *
     * @param gameState the saved game state to load
     */
    public void goToGame(GameState gameState) {
        if (gameState == null) {
            goToMenu();
            return;
        }
        loadProgressionFromGameState(gameState);
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        currentLevelNumber = gameState.getLevel();
        gameScreen = new GameScreen(this, gameState);
        this.setScreen(gameScreen);
    }


    /**
     * Switches to the new game screen.
     */
    public void goToNewGameScreen() {
        this.setScreen(new NewGameScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Switches to the first cutscene screen.
     */
    public void goToCutsceneScreen() {
        this.setScreen(new CutsceneScreen(this));
    }

    /**
     * Switches to the second cutscene screen.
     *
     * @param targetLevel the level number used by the cutscene
     */
    public void goToSecondCutsceneScreen(int targetLevel) {
        this.setScreen(new SecondCutsceneScreen(this, targetLevel));
    }

    /**
     * Switches to the continue game screen.
     */
    public void goToContinueGameScreen() {
        this.setScreen(new ContinueGameScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Switches to the level select screen.
     */
    public void goToLevelSelectScreen() {
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }
        this.setScreen(levelSelectScreen);
    }

    /**
     * Switches to the settings screen and remembers the current screen.
     */
    public void goToSettingsScreen() {
        Screen current = getScreen();
        if (!(current instanceof SettingsScreen
                || current instanceof SettingsAudioScreen
                || current instanceof SettingsVideoScreen
                || current instanceof SettingsControlsScreen
                || current instanceof SettingsGameScreen)) {
            settingsReturnScreen = current;
        }
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen(this);
        }
        this.setScreen(settingsScreen);
    }

    /**
     * Returns from settings to the previously active screen.
     */
    public void goBackFromSettings() {
        if (settingsReturnScreen instanceof SurvivalScreen && survivalScreen != null) {
            this.setScreen(survivalScreen);
            return;
        }
        if (settingsReturnScreen instanceof GameScreen && gameScreen != null) {
            this.setScreen(gameScreen);
            return;
        }
        if (settingsReturnScreen instanceof MenuScreen && menuScreen != null) {
            this.setScreen(menuScreen);
            return;
        }
        goToMenu();
    }

    /**
     * Switches to the highscore screen.
     */
    public void goToHighscoreScreen() {
        if (highscoreScreen == null) {
            highscoreScreen = new HighscoreScreen(this);
        }
        this.setScreen(highscoreScreen);
    }

    /**
     * Switches to the progression tree screen from gameplay.
     */
    public void goToProgressionTreeScreenFromGame() {
        if (progressionTreeScreen == null) {
            progressionTreeScreen = new ProgressionTreeScreen(this);
        }
        this.setScreen(progressionTreeScreen);
    }

    /**
     * Returns from the progression tree to the previous game-related screen.
     */
    public void goBackFromProgressionTree() {
        if (gameScreen != null) {
            this.setScreen(gameScreen);
        } else if (survivalScreen != null) {
            this.setScreen(survivalScreen);
        } else {
            goToMenu();
        }
    }

    /**
     * Switches to the settings controls screen.
     */
    public void goToSettingsControlsScreen() {
        this.setScreen(new SettingsControlsScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the achievements screen.
     */
    public void goToAchievementsScreen() {
        this.setScreen(new AchievementsScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Switches to the settings video screen.
     */
    public void goToSettingsVideoScreen() {
        this.setScreen(new SettingsVideoScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the settings audio screen.
     */
    public void goToSettingsAudioScreen() {
        this.setScreen(new SettingsAudioScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the settings game screen.
     */
    public void goToSettingsGameScreen() {
        this.setScreen(new SettingsGameScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the generic game over screen.
     */
    public void goToGameOverScreen() {
        this.setScreen(new GameOverScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the death-over screen for BMW-related death.
     */
    public void goToDeathOverScreen() {
        this.setScreen(new DeathOverScreen(this));
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the pothole death screen.
     */
    public void goToPotholeDeathScreen() {
        this.setScreen(new PotholeDeathScreen(this));
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the BMW explosion death screen.
     */
    public void goToBmwExplosionDeathScreen() {
        this.setScreen(new BmwExplosionDeathScreen(this));
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the victory screen and awards progression points.
     */
    public void goToVictoryScreen() {
        progressionManager.addPoints(500);
        this.setScreen(new VictoryScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Handles player death by switching to the appropriate screen.
     *
     * @param cause the cause of death, or {@code null} for a generic game over
     */
    public void handlePlayerDeath(DeathCause cause) {
        if (cause == null) {
            goToGameOverScreen();
            return;
        }
        switch (cause) {
            case BMW -> goToDeathOverScreen();
            case BMW_EXPLOSION -> goToBmwExplosionDeathScreen();
            case POTHOLE -> goToPotholeDeathScreen();
            case TIMEOUT, ARRESTED -> goToGameOverScreen();
        }
    }

    /**
     * Disposes all active screens and shared resources.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose of the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose of the skin
        audioManager.dispose();
        if (achievementPopupScreen != null) {
            achievementPopupScreen.dispose();
        }
    }

    /**
     * Renders the current screen and the achievement popup overlay.
     */
    @Override
    public void render() {
        Screen current = getScreen();
        if (shouldRenderMenuBackground() && !(current instanceof MenuScreen)) {
            if (menuScreen == null) {
                menuScreen = new MenuScreen(this);
            }
            menuScreen.renderBackground(Gdx.graphics.getDeltaTime());
        }
        super.render();

        // So its not tied to a screen
        if (achievementPopupScreen != null) {
            achievementPopupScreen.act(Gdx.graphics.getDeltaTime());
            achievementPopupScreen.draw();
        }
    }

    /**
     * Resizes the game and the achievement popup overlay.
     *
     * @param width new width in pixels
     * @param height new height in pixels
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (achievementPopupScreen != null) {
            achievementPopupScreen.resize(width, height);
        }
    }

    // Getter methods
    /**
     * Returns the shared UI skin.
     *
     * @return UI skin
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Returns the shared sprite batch.
     *
     * @return sprite batch
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    /**
     * Returns the configuration manager.
     *
     * @return configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Returns the key handler.
     *
     * @return key handler
     */
    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    /**
     * Returns the current level number.
     *
     * @return current level number
     */
    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    /**
     * Returns the progression manager.
     *
     * @return progression manager
     */
    public ProgressionManager getProgressionManager() {
        return progressionManager;
    }

    /**
     * Starts a new progression run with default points.
     */
    public void startNewGameProgression() {
        progressionManager = new ProgressionManager(2000);
    }

    /**
     * Returns the audio manager.
     *
     * @return audio manager
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * Returns the current game screen.
     *
     * @return game screen, or {@code null} if not initialized
     */
    public GameScreen getGameScreen() {
        return gameScreen;
    }

    /**
     * Returns the current survival screen.
     *
     * @return survival screen, or {@code null} if not initialized
     */
    public SurvivalScreen getSurvivalScreen() {
        return survivalScreen;
    }

    /**
     * Returns the graphics manager.
     *
     * @return graphics manager
     */
    public GraphicsManager getGraphicsManager() {
        return graphicsManager;
    }

    /**
     * Determines whether the menu background should be rendered behind other screens.
     *
     * @return {@code true} if the menu background should render
     */
    public boolean shouldRenderMenuBackground() {
        Screen current = getScreen();
        return !(current instanceof GameScreen) && !(current instanceof SurvivalScreen);
    }

    /**
     * Loads progression data from a saved game state.
     *
     * @param gameState saved game state to read from
     */
    private void loadProgressionFromGameState(GameState gameState) {
        int points = gameState.getProgressionPoints();
        if (gameState.getOwnedUpgrades() == null) {
            points = 2000;
        }
        ProgressionManager loaded = new ProgressionManager(points);
        loaded.setOwnedUpgrades(gameState.getOwnedUpgrades());
        progressionManager = loaded;
    }
}
