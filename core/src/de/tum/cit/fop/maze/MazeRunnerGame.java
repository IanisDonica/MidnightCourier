package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.fop.maze.screen.*;
import de.tum.cit.fop.maze.system.ConfigManager;
import de.tum.cit.fop.maze.system.GameState;
import de.tum.cit.fop.maze.system.SaveManager;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import java.io.ObjectInputFilter;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */

public class MazeRunnerGame extends Game {
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SettingsScreen settingsScreen;
    private SpriteBatch spriteBatch;
    private Skin skin;
    private final ConfigManager configManager;

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in a desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        configManager = new ConfigManager();
        configManager.loadKeyBindings();
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin

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
        this.setScreen(new MenuScreen(this)); // Set the current screen to MenuScreen
        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the game screen if it exists
            gameScreen = null;
        }
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
    }

    public void goToGame(GameState gameState){
        this.setScreen(new GameScreen(this, gameState)); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }
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

    /**
     * Switches to the settings screen.
     */
    public void goToSettingsScreen() {
        this.setScreen(new SettingsScreen(this)); // Set the current screen to GameScreen
        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
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
        this.setScreen(new SettingsScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToSettingsAudioScreen() {
        this.setScreen(new SettingsScreen(this)); // Set the current screen to GameScreen
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToSettingsGameScreen() {
        this.setScreen(new SettingsControlsScreen(this)); // Set the current screen to GameScreen
        if(settingsScreen != null){
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToGameOverScreen() {
        this.setScreen(new GameOverScreen(this)); // Set the current screen to GameScreen
        if(settingsScreen != null){
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToVictoryScreen() {
        this.setScreen(new VictoryScreen(this)); // Set the current screen to GameScreen
        if(settingsScreen != null){
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

}
