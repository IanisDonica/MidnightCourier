package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.screen.*;

public class KeyHandler extends InputListener {
    private final MazeRunnerGame game;
    private final ConfigManager configManager;
    private final AudioManager audioManager;
    private Player player;

    public KeyHandler(MazeRunnerGame game) {
        this.game = game;
        configManager = game.getConfigManager();
        audioManager = game.getAudioManager();
    }

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        return handleKey(keycode, true);
    }

    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        return handleKey(keycode, false);
    }

    private boolean handleKey(int keycode, boolean isDown) {
        // Player movement and sprint
        if (player != null && checkMovementKeys(keycode, isDown)) return true;

        // GameScreen debug, menu and effects (only on keyDown)
        if (isDown) {
            return handlePauseKey(keycode) || handleScreenEffects(keycode);
        }

        return false;
    }

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

    private boolean handlePauseKey(int keycode) {
        if (keycode == configManager.getKeyBinding("pause")) {
            audioManager.playSound("Click.wav", 1);
            Screen s = game.getScreen();
            if (s instanceof GameScreen || s instanceof SurvivalScreen) {
                game.pause();
            } else if (s instanceof SettingsScreen && game.getGameScreen() != null && game.getGameScreen().isPaused()) {
                game.goToGame();
            } else if (s instanceof SettingsGameScreen || s instanceof SettingsVideoScreen || s instanceof SettingsAudioScreen || s instanceof SettingsControlsScreen) {
                game.goToSettingsScreen();
            } else {
                game.goToMenu();
            }
            return true;
        }
        return false;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
