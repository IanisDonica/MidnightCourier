package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.PointManager;

/**
 * Collectible that restores player health.
 */
public class HealthPickup extends Collectible {
    /**
     * Creates a health pickup.
     *
     * @param x spawn x position
     * @param y spawn y position
     * @param pointManager point manager for scoring
     */
    public HealthPickup(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        initSpinAnimation(0, 64, 4);
    }

    /**
     * Handles collision by healing the player and awarding points.
     */
    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            this.player.setHp(this.player.getHp() + 1);
            pointManager.add(400);
            markPickedUp();
        }
    }
}
