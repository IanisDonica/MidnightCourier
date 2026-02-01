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
    /** False if the last screen is gamescreen, true if the last screen is survivalScreen. */
    private boolean selectedScreen;
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
    /** Screen to return to after leaving the continue game screen. */
    private Screen continueReturnScreen;


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

        achievementPopupScreen = new AchievementPopupScreen(this);
        AchievementManager.setPopupScreen(achievementPopupScreen);
        progressionManager = new ProgressionManager(200);
        audioManager.preloadSounds("Click.wav", "pickup.wav", "siren.ogg", "decelerating.wav", "pedal.wav", "pickup.wav", "tires.wav", "tires_loop.wav");
        audioManager.playMusic("True_love.mp3", 1f, true);
        graphicsManager.load();
        graphicsManager.applySettings();
        goToMenu(); // Navigate to the menu screen
    }


    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        if (gameScreen != null) {
            SaveManager.saveGame(gameScreen.getGameState());
            SaveManager.saveInfo(false, 0);
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen != null) {
            SaveManager.saveGame(survivalScreen.getGameState());
            SaveManager.saveInfo(true, 0);
            survivalScreen.dispose();
            survivalScreen = null;
        }
        audioManager.stopAllSounds();
        audioManager.stopPlaylist();
        audioManager.playMusic("True_love.mp3", 1f, true);

        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        this.setScreen(menuScreen);
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        audioManager.stopAllSounds();
        audioManager.stopMusic();
        playDefaultPlaylist();


        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        if (gameScreen == null) {
            gameScreen = new GameScreen(this, Math.max(currentLevelNumber, 1));
        }
        audioManager.stopAllSounds();
        this.selectedScreen = false;
        this.setScreen(gameScreen);
    }

    /**
     * Switches to endless mode from the current screen.
     */
    public void goToEndless() {
        audioManager.stopAllSounds();
        audioManager.stopMusic();
        playDefaultPlaylist();
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen == null) {
            currentLevelNumber = 0;
            survivalScreen = new SurvivalScreen(this);
        }
        this.selectedScreen = true;
        this.setScreen(survivalScreen);
    }

    /**
     * Switches to endless mode with a specific game state.
     *
     * @param gameState the state of the loaded game
     */
    public void goToEndless(GameState gameState) {
        audioManager.stopAllSounds();
        audioManager.stopMusic();
        playDefaultPlaylist();
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

        this.selectedScreen = true;
        currentLevelNumber = gameState.getLevel();
        survivalScreen = new SurvivalScreen(this, gameState);
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
        audioManager.stopAllSounds();
        audioManager.stopMusic();
        playDefaultPlaylist();
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (survivalScreen != null) {
            survivalScreen.dispose();
            survivalScreen = null;
        }
        this.selectedScreen = false;
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
        audioManager.stopAllSounds();
        audioManager.stopMusic();
        playDefaultPlaylist();
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
        this.selectedScreen = false;
        currentLevelNumber = gameState.getLevel();
        gameScreen = new GameScreen(this, gameState);
        this.setScreen(gameScreen);
    }


    /**
     * Switches to the new game screen.
     */
    public void goToNewGameScreen() {
        audioManager.stopAllSounds();
        this.setScreen(new NewGameScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Switches to the first cutscene screen.
     */
    public void goToCutsceneScreen() {
        audioManager.stopAllSounds();
        audioManager.stopPlaylist();
        this.setScreen(new CutsceneScreen(this));
    }

    /**
     * Switches to the second cutscene screen.
     *
     * @param targetLevel the level number used by the cutscene
     */
    public void goToSecondCutsceneScreen(int targetLevel) {
        audioManager.stopAllSounds();
        audioManager.stopPlaylist();d
        this.setScreen(new SecondCutsceneScreen(this, targetLevel));
    }

    /**
     * Switches to the continue game screen.
     */
    public void goToContinueGameScreen() {
        audioManager.stopAllSounds();
        Screen current = getScreen();
        if (!(current instanceof MenuScreen) && !(current instanceof ContinueGameScreen)) {
            continueReturnScreen = current;
        } else {
            continueReturnScreen = null;
        }
        this.setScreen(new ContinueGameScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Returns from the continue game screen to the previous screen when available.
     */
    public void goBackFromContinueGame() {
        audioManager.stopAllSounds();
        if (continueReturnScreen != null) {
            this.setScreen(continueReturnScreen);
            continueReturnScreen = null;
        } else {
            goToMenu();
        }
    }

    /**
     * Switches to the level select screen.
     */
    public void goToLevelSelectScreen() {
        audioManager.stopAllSounds();
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }
        this.setScreen(levelSelectScreen);
    }

    /**
     * Switches to the settings screen and remembers the current screen.
     */
    public void goToSettingsScreen() {
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
        if (highscoreScreen == null) {
            highscoreScreen = new HighscoreScreen(this);
        }
        this.setScreen(highscoreScreen);
    }

    /**
     * Switches to the progression tree screen from gameplay.
     */
    public void goToProgressionTreeScreenFromGame() {
        audioManager.stopAllSounds();
        if (progressionTreeScreen == null) {
            progressionTreeScreen = new ProgressionTreeScreen(this);
        }
        this.setScreen(progressionTreeScreen);
    }

    /**
     * Returns from the progression tree to the previous game-related screen.
     */
    public void goBackFromProgressionTree() {
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
        this.setScreen(new AchievementsScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Switches to the settings video screen.
     */
    public void goToSettingsVideoScreen() {
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
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
        audioManager.stopAllSounds();
        this.setScreen(new BmwExplosionDeathScreen(this));
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    /**
     * Switches to the timeout (fired) death screen.
     */
    public void goToFiredScreen() {
        audioManager.stopAllSounds();
        this.setScreen(new FiredScreen(this));
        if (settingsScreen != null) {
            settingsScreen.dispose();
            settingsScreen = null;
        }
    }

    /**
     * Switches to the victory screen and awards progression points.
     */
    public void goToVictoryScreen() {
        audioManager.stopAllSounds();
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
            case TIMEOUT -> goToFiredScreen();
            case ARRESTED -> goToGameOverScreen();
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
        progressionManager = new ProgressionManager(200);
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

    public boolean getSelectedScreen(){
        return selectedScreen;
    }

    private void playDefaultPlaylist(){
        audioManager.playPlaylist(1f, true,
                "Drift_City.mp3",
                "Midnight_Ride.mp3",
                "No_More_Heroes_2.mp3",
                "one_last_drift.mp3",
                "Race_to_Mt._Fuji.mp3",
                "RETROPINK.mp3",
                "shade_drift_phonk.mp3",
                "Chase_Scene.mp3"
        );
    }

    /**
     * Loads progression data from a saved game state.
     *
     * @param gameState saved game state to read from
     */
    private void loadProgressionFromGameState(GameState gameState) {
        int points = gameState.getProgressionPoints();
        if (gameState.getOwnedUpgrades() == null) {
            points = 200;
        }
        ProgressionManager loaded = new ProgressionManager(points);
        loaded.setOwnedUpgrades(gameState.getOwnedUpgrades());
        progressionManager = loaded;
    }
}
