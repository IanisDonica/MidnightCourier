package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.screen.*;
import de.tum.cit.fop.maze.system.ConfigManager;
import de.tum.cit.fop.maze.system.GameState;
import de.tum.cit.fop.maze.system.KeyHandler;
import de.tum.cit.fop.maze.system.ProgressionManager;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */

public class MazeRunnerGame extends Game {
    private final NativeFileChooser fileChooser;
    private final ConfigManager configManager;
    private final KeyHandler keyHandler;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SettingsScreen settingsScreen;
    private LevelSelectScreen levelSelectScreen;
    private HighscoreScreen highscoreScreen;
    private ProgressionTreeScreen progressionTreeScreen;
    private int currentLevelNumber = 1;
    private SpriteBatch spriteBatch;
    private Skin skin;
    private ProgressionManager progressionManager;

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
        configManager = new ConfigManager();
        keyHandler = new KeyHandler(this);
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        configManager.loadKeyBindings();
        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        progressionManager = new ProgressionManager(2000);

        // Play some background music
        // Background sound
        // Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        // backgroundMusic.setLooping(true);
        // backgroundMusic.play();

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
            gameScreen = new GameScreen(this);
        }
        this.setScreen(gameScreen);
    }

    public void goToGame(int levelNumber) {
        // This will be the case after Try again/Next level
        // If this is not done, there will be a memory leak
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        currentLevelNumber = levelNumber;
        gameScreen = new GameScreen(this, currentLevelNumber);
        this.setScreen(gameScreen);
    }

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
        currentLevelNumber = gameState.getLevel();
        gameScreen = new GameScreen(this, gameState);
        this.setScreen(gameScreen);
    }


    /**
     * Switches to the new game screen.
     */
    public void goToNewGameScreen() {
        this.setScreen(new NewGameScreen(this)); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
    }

    /**
     * Switches to the continue game screen.
     */
    public void goToContinueGameScreen() {
        this.setScreen(new ContinueGameScreen(this)); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
    }

    public void goToLevelSelectScreen() {
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }
        this.setScreen(levelSelectScreen);
    }

    public void goToSettingsScreen() {
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen(this);
        }
        this.setScreen(settingsScreen);
    }

    public void goToHighscoreScreen() {
        if (highscoreScreen == null) {
            highscoreScreen = new HighscoreScreen(this);
        }
        this.setScreen(highscoreScreen);
    }

    public void goToProgressionTreeScreenFromGame() {
        if (progressionTreeScreen == null) {
            progressionTreeScreen = new ProgressionTreeScreen(this);
        }
        this.setScreen(progressionTreeScreen);
    }

    public void goBackFromProgressionTree() {
        if (gameScreen != null) {
            this.setScreen(gameScreen);
        } else {
            goToMenu();
        }
    }

    public void goToSettingsControlsScreen() {
        this.setScreen(new SettingsControlsScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToSettingsVideoScreen() {
        this.setScreen(new SettingsVideoScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToSettingsAudioScreen() {
        this.setScreen(new SettingsAudioScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToSettingsGameScreen() {
        this.setScreen(new SettingsGameScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToGameOverScreen() {
        this.setScreen(new GameOverScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToVictoryScreen() {
        this.setScreen(new VictoryScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose of the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose of the skin
    }

    // Getter methods
    public Skin getSkin() {
        return skin;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public ProgressionManager getProgressionManager() {
        return progressionManager;
    }

    public void startNewGameProgression() {
        progressionManager = new ProgressionManager(2000);
    }

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
