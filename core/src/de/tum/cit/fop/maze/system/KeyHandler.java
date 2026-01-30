package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.screen.*;

/**
 * Handles key input and routes actions to the appropriate screens.
 */
public class KeyHandler extends InputListener {
    /** Game instance used for screen navigation. */
    private final MazeRunnerGame game;
    /** Configuration manager for key bindings. */
    private final ConfigManager configManager;
    /** Audio manager for click sounds. */
    private final AudioManager audioManager;
    /** Current player instance for movement input. */
    private Player player;

    /**
     * Creates a key handler for the given game.
     *
     * @param game game instance
     */
    public KeyHandler(MazeRunnerGame game) {
        this.game = game;
        configManager = game.getConfigManager();
        audioManager = game.getAudioManager();
    }

    /**
     * Handles key-down events.
     *
     * @param event input event
     * @param keycode key code
     * @return {@code true} if the event was handled
     */
    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        return handleKey(keycode, true);
    }

    /**
     * Handles key-up events.
     *
     * @param event input event
     * @param keycode key code
     * @return {@code true} if the event was handled
     */
    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        return handleKey(keycode, false);
    }

    /**
     * Handles scroll events for zoom control.
     *
     * @param event input event
     * @param x scroll x
     * @param y scroll y
     * @param amountX scroll amount x
     * @param amountY scroll amount y
     * @return {@code true} if the event was handled
     */
    @Override
    public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
        Screen s = game.getScreen();
        GameScreen gameScreen = (s instanceof GameScreen) ? game.getGameScreen() : null;
        SurvivalScreen survivalScreen = (s instanceof SurvivalScreen) ? game.getSurvivalScreen() : null;
        if (gameScreen == null && survivalScreen == null) {
            return false;
        }
        float zoomDelta = amountY * 0.02f;
        if (gameScreen != null) {
            gameScreen.adjustZoom(zoomDelta);
        } else {
            survivalScreen.adjustZoom(zoomDelta);
        }
        return true;
    }

    /**
     * Routes key input between movement and screen commands.
     *
     * @param keycode key code
     * @param isDown whether the key is pressed
     * @return {@code true} if handled
     */
    private boolean handleKey(int keycode, boolean isDown) {
        // Player movement and sprint
        if (player != null && checkMovementKeys(keycode, isDown)) return true;

        // GameScreen debug, menu and effects (only on keyDown)
        if (isDown) {
            return handlePauseKey(keycode) || handleScreenEffects(keycode);
        }

        return false;
    }

    /**
     * Handles movement and sprint keys for the player.
     *
     * @param keycode key code
     * @param isDown whether the key is pressed
     * @return {@code true} if handled
     */
    private boolean checkMovementKeys(int keycode, boolean isDown) {
        if (keycode == configManager.getKeyBinding("up")) {
            player.setMoveUp(isDown);
            return true;
        }
        if (keycode == configManager.getKeyBinding("down")) {
            player.setMoveDown(isDown);
            return true;
        }
        if (keycode == configManager.getKeyBinding("left")) {
            player.setMoveLeft(isDown);
            return true;
        }
        if (keycode == configManager.getKeyBinding("right")) {
            player.setMoveRight(isDown);
            return true;
        }
        if (keycode == configManager.getKeyBinding("sprint")) {
            player.setSprinting(isDown);
            return true;
        }
        return false;
    }

    /**
     * Handles non-pause screen effects (dev console, shop, zoom, fog).
     *
     * @param keycode key code
     * @return {@code true} if handled
     */
    private boolean handleScreenEffects(int keycode) {
        Screen s = game.getScreen();
        GameScreen gameScreen = (s instanceof GameScreen) ? game.getGameScreen() : null;
        SurvivalScreen survivalScreen = (s instanceof SurvivalScreen) ? game.getSurvivalScreen() : null;
        if (gameScreen == null && survivalScreen == null) return false;
        if (keycode == Input.Keys.GRAVE && (s instanceof GameScreen || s instanceof SurvivalScreen)) {
            audioManager.playSound("Click.wav", 1);
            if (gameScreen != null) {
                gameScreen.toggleDevConsole();
            } else {
                survivalScreen.toggleDevConsole();
            }
            return true;
        }

        if (keycode == configManager.getKeyBinding("openShop")
                && (s instanceof GameScreen || s instanceof SurvivalScreen)
                && gameScreen != null
                && gameScreen.getHud().isShopButtonVisible()) {
            audioManager.playSound("Click.wav", 1);
            game.goToProgressionTreeScreenFromGame();
            return true;
        }
        if (keycode == configManager.getKeyBinding("zoomIn")) {
            audioManager.playSound("Click.wav", 1);
            if (gameScreen != null) {
                gameScreen.adjustZoom(-0.02f);
            } else {
                survivalScreen.adjustZoom(-0.02f);
            }
            return true;
        }
        if (keycode == configManager.getKeyBinding("zoomOut")) {
            audioManager.playSound("Click.wav", 1);
            if (gameScreen != null) {
                gameScreen.adjustZoom(0.02f);
            } else {
                survivalScreen.adjustZoom(0.02f);
            }
            return true;
        }
        if (keycode == configManager.getKeyBinding("moreFog")) {
            audioManager.playSound("Click.wav", 1);
            if (gameScreen != null) {
                gameScreen.adjustFog(-0.5f);
            } else {
                survivalScreen.adjustFog(-0.5f);
            }
            return true;
        }
        if (keycode == configManager.getKeyBinding("lessFog")) {
            audioManager.playSound("Click.wav", 1);
            if (gameScreen != null) {
                gameScreen.adjustFog(0.5f);
            } else {
                survivalScreen.adjustFog(0.5f);
            }
            return true;
        }
        if (keycode == configManager.getKeyBinding("noire")) {
            audioManager.playSound("Click.wav", 1);
            if (gameScreen != null) {
                gameScreen.toggleNoireMode();
            } else {
                survivalScreen.toggleNoireMode();
            }
            return true;
        }
        return false;
    }

    /**
     * Handles pause and navigation keys.
     *
     * @param keycode key code
     * @return {@code true} if handled
     */
    private boolean handlePauseKey(int keycode) {
        if (keycode == configManager.getKeyBinding("pause")) {
            audioManager.playSound("Click.wav", 1);
            Screen s = game.getScreen();
            if (s instanceof CutsceneScreen || s instanceof SecondCutsceneScreen) {
                return true;
            }
            if (s instanceof GameScreen || s instanceof SurvivalScreen) {
                game.pause();
            } else if (s instanceof ProgressionTreeScreen) {
                game.goBackFromProgressionTree();
            } else if (s instanceof SettingsScreen) {
                game.goBackFromSettings();
            } else if (s instanceof SettingsGameScreen || s instanceof SettingsVideoScreen || s instanceof SettingsAudioScreen || s instanceof SettingsControlsScreen) {
                game.goToSettingsScreen();
            } else if (s instanceof PotholeDeathScreen || s instanceof DeathOverScreen || s instanceof GameOverScreen || s instanceof BmwExplosionDeathScreen) {
                return true;
            } else {
                game.goToMenu();
            }
            return true;
        }
        return false;
    }

    /**
     * Sets the player used for movement input.
     *
     * @param player player instance
     */
    public void setPlayer(Player player) {
        this.player = player;
    }
}
