package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.screen.*;

public class KeyHandler extends InputListener {
    private final MazeRunnerGame game;
    private final ConfigManager configManager;
    private Player player;
    private GameScreen gameScreen;

    public KeyHandler(MazeRunnerGame game) {
        this.game = game;
        configManager = game.getConfigManager();
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

        // GameScreen debug and menu (only on keyDown)
        // TODO fix
        if (isDown) {
            if (keycode == configManager.getKeyBinding("pause")) {
                if (game.getScreen() instanceof GameScreen) {
                    game.goToMenu();
                } else if (game.getScreen() instanceof MenuScreen ||
                        game.getScreen() instanceof SettingsScreen ||
                        game.getScreen() instanceof SettingsControlsScreen) {
                    game.goToGame();
                }
                return true;
            }
            if (keycode == configManager.getKeyBinding("zoomIn")) {
                gameScreen.adjustZoom(0.02f);
                return true;
            }
            if (keycode == configManager.getKeyBinding("zoomOut")) {
                gameScreen.adjustZoom(-0.02f);
                return true;
            }
            if (keycode == configManager.getKeyBinding("moreFog")) {
                gameScreen.adjustFog(0.5f);
                return true;
            }
            if (keycode == configManager.getKeyBinding("lessFog")) {
                gameScreen.adjustFog(-0.5f);
                return true;
            }
            if (keycode == configManager.getKeyBinding("noire")) {
                gameScreen.toggleNoireMode();
                return true;
            }
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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
}
