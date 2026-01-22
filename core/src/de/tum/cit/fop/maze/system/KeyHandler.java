package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.screen.GameScreen;

public class KeyHandler extends InputListener {
    private final Player player;
    private final GameScreen gameScreen;
    private final MazeRunnerGame game;
    private final ConfigManager configManager;

    public KeyHandler(Player player, GameScreen gameScreen, MazeRunnerGame game) {
        this.player = player;
        this.gameScreen = gameScreen;
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
        if (checkMovementKeys(keycode, isDown)) return true;

        // GameScreen debug and menu (only on keyDown)
        if (isDown) {
            if (keycode == configManager.getKeyBinding("pause")) {
                game.goToMenu();
                return true;
            }
            if (keycode == configManager.getKeyBinding("zoomIn")) {
                gameScreen.adjustZoom(0.1f);
                return true;
            }
            if (keycode == configManager.getKeyBinding("zoomOut")) {
                gameScreen.adjustZoom(-0.1f);
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
}
