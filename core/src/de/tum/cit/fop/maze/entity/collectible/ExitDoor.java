package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.system.PointManager;

/**
 * Exit door that completes a level when the player has the key.
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
     * @param victoryListener listener to call on victory
     */
    public ExitDoor(float x, float y, PointManager pointManager, VictoryListener victoryListener) {
        super(x, y, 1, 1, pointManager);
        this.victoryListener = victoryListener;
        // Placeholder chest, should no animation be needed frame count can be set to 1
        initSpinAnimation(0, 0, 2);
    }

    /**
     * Handles player collision and triggers victory if the key is owned.
     */
    @Override
    protected void collision() {
        if (triggered) {
            return;
        }
        if (this.player.hasKey()) {
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
}
