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
    private final int[] moveUpKeys = {Input.Keys.W, Input.Keys.UP};
    private final int[] moveDownKeys = {Input.Keys.S, Input.Keys.DOWN};
    private final int[] moveLeftKeys = {Input.Keys.A, Input.Keys.LEFT};
    private final int[] moveRightKeys = {Input.Keys.D, Input.Keys.RIGHT};
    private final int[] sprintKeys = {Input.Keys.SHIFT_LEFT};

    public KeyHandler(Player player, GameScreen gameScreen, MazeRunnerGame game) {
        this.player = player;
        this.gameScreen = gameScreen;
        this.game = game;
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
            if (keycode == Input.Keys.ESCAPE) {
                game.goToMenu();
                return true;
            }
            if (keycode == Input.Keys.NUMPAD_ADD) {
                gameScreen.adjustZoom(0.1f);
                return true;
            }
            if (keycode == Input.Keys.NUMPAD_SUBTRACT) {
                gameScreen.adjustZoom(-0.1f);
                return true;
            }
            if (keycode == Input.Keys.NUMPAD_9) {
                gameScreen.adjustFog(0.5f);
                return true;
            }
            if (keycode == Input.Keys.NUMPAD_8) {
                gameScreen.adjustFog(-0.5f);
                return true;
            }
            if (keycode == Input.Keys.NUMPAD_7) {
                gameScreen.toggleNoireMode();
                return true;
            }
        }

        return false;
    }

    private boolean checkMovementKeys(int keycode, boolean isDown) {
        for (int key : moveUpKeys) {
            if (key == keycode) {
                player.setMoveUp(isDown);
                return true;
            }
        }
        for (int key : moveDownKeys) {
            if (key == keycode) {
                player.setMoveDown(isDown);
                return true;
            }
        }
        for (int key : moveLeftKeys) {
            if (key == keycode) {
                player.setMoveLeft(isDown);
                return true;
            }
        }
        for (int key : moveRightKeys) {
            if (key == keycode) {
                player.setMoveRight(isDown);
                return true;
            }
        }
        for (int key : sprintKeys) {
            if (key == keycode) {
                player.setSprinting(isDown);
                return true;
            }
        }
        return false;
    }
}
