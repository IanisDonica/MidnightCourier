package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.PointManager;

public class Key extends Collectible {
    public Key(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        //Placeholder fire texture
        initSpinAnimation(64, 48, 4);
    }

    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            pointManager.add(2000);
            player.pickupKey();
            markPickedUp();
        }
    }
}
