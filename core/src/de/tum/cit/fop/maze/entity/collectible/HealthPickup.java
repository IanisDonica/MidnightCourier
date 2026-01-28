package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.PointManager;

public class HealthPickup extends Collectible {
    public HealthPickup(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        initSpinAnimation(0, 64, 4);
    }

    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            this.player.setHp(this.player.getHp() + 1);
            pointManager.add(400);
            markPickedUp();
        }
    }
}
