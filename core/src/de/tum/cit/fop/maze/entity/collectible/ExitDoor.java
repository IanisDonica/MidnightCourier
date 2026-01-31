package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.system.PointManager;

/**
 * Exit door that grants permission to leave when the player has the key.
 */
public class ExitDoor extends Collectible {
    // The door is obviously not a Collectible, but it's easier to implement it as such due to the colision code
    /**
     * Listener invoked when victory is triggered.
     */
    public interface VictoryListener {
        void onVictory();
    }

    /** Listener to notify on victory. */
    private final VictoryListener victoryListener;
    /** Whether the exit has been triggered. */
    private boolean triggered = false;

    /**
     * Creates an exit door collectible.
     *
     * @param x spawn x position
     * @param y spawn y position
     * @param pointManager point manager for scoring
     */
    public ExitDoor(float x, float y, PointManager pointManager, VictoryListener victoryListener) {
        super(x, y, 1, 1, pointManager);
        this.victoryListener = victoryListener;
        Texture texture = new Texture(Gdx.files.internal("RoadBlock.png"));
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        frames.add(new TextureRegion(texture));
        spinAnimation = new Animation<>(1f, frames, Animation.PlayMode.NORMAL);
        animationTime = 0f;
    }

    /**
     * Updates visibility based on whether the player can leave.
     *
     * @param delta frame delta time
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        if (player != null) {
            setVisible(!player.canLeave());
        }
    }

    /**
     * Handles player collision and triggers victory if leaving is allowed.
     */
    @Override
    protected void collision() {
        if (triggered) {
            return;
        }
        if (!this.player.canLeave()) {
            return;
        }
        triggered = true;
        this.pointManager.saveScore(this.player.getHp());
        AchievementManager.incrementProgress("first_delivery", 1);
        AchievementManager.incrementProgress("complete_100_deliveries", 1);
        if (this.pointManager.getLevel() == 5) {
            AchievementManager.incrementProgress("finish_level_5", 1);
        }
        if (victoryListener != null) {
            victoryListener.onVictory();
        }
    }
}
