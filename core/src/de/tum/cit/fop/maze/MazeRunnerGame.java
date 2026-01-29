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
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */

public class MazeRunnerGame extends Game {
    private final ConfigManager configManager;
    private final KeyHandler keyHandler;
    private final AudioManager audioManager;
    private final GraphicsManager graphicsManager;
    private AchievementPopupScreen achievementPopupScreen;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SurvivalScreen survivalScreen;
    private SettingsScreen settingsScreen;
    private LevelSelectScreen levelSelectScreen;
    private HighscoreScreen highscoreScreen;
    private ProgressionTreeScreen progressionTreeScreen;
    private int currentLevelNumber = 1;
    private SpriteBatch spriteBatch;
    private Skin skin;
    private ProgressionManager progressionManager;
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

    public void goToCutsceneScreen() {
        this.setScreen(new CutsceneScreen(this));
    }

    public void goToSecondCutsceneScreen(int targetLevel) {
        this.setScreen(new SecondCutsceneScreen(this, targetLevel));
    }

    /**
     * Switches to the continue game screen.
     */
    public void goToContinueGameScreen() {
        this.setScreen(new ContinueGameScreen(this)); // Set the current screen to GameScreen
    }

    public void goToLevelSelectScreen() {
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }
        this.setScreen(levelSelectScreen);
    }

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
        } else if (survivalScreen != null) {
            this.setScreen(survivalScreen);
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

    public void goToAchievementsScreen() {
        this.setScreen(new AchievementsScreen(this)); // Set the current screen to GameScreen
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

    public void goToDeathOverScreen() {
        this.setScreen(new DeathOverScreen(this));
        if (settingsScreen != null) {
            settingsScreen.dispose(); // Dispose the menu screen if it exists
            settingsScreen = null;
        }
    }

    public void goToVictoryScreen() {
        progressionManager.addPoints(500);
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
        audioManager.dispose();
        if (achievementPopupScreen != null) {
            achievementPopupScreen.dispose();
        }
    }

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

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (achievementPopupScreen != null) {
            achievementPopupScreen.resize(width, height);
        }
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

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public SurvivalScreen getSurvivalScreen() {
        return survivalScreen;
    }

    public GraphicsManager getGraphicsManager() {
        return graphicsManager;
    }

    public boolean shouldRenderMenuBackground() {
        Screen current = getScreen();
        return !(current instanceof GameScreen) && !(current instanceof SurvivalScreen);
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
