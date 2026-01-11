package de.tum.cit.fop.maze;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class KeyHandler extends InputListener {
    private final Player player;
    private final GameScreen gameScreen;
    private final MazeRunnerGame game;
    private final int moveUpKeys = Input.Keys.W;
    private final int moveDownKeys = Input.Keys.S;
    private final int moveLeftKeys = Input.Keys.A;
    private final int moveRightKeys = Input.Keys.D;
    private final int sprintKeys = Input.Keys.SHIFT_LEFT;

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
        if (keycode == moveUpKeys) {
            player.setMoveUp(isDown);
            return true;
        } else if (keycode == moveDownKeys) {
            player.setMoveDown(isDown);
            return true;
        } else if (keycode == moveLeftKeys) {
            player.setMoveLeft(isDown);
            return true;
        } else if (keycode == moveRightKeys) {
            player.setMoveRight(isDown);
            return true;
        } else if (keycode == sprintKeys) {
            player.setSprinting(isDown);
            return true;
        }

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
}
