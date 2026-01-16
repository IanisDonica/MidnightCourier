package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.PointManager;

public class EnergyDrink extends Collectible {
    public EnergyDrink(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        initSpinAnimation(0, 64, 4);
    }

    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            this.player.drinkEnergyDrink();
            pointManager.add(50);
            markPickedUp();
        }
    }
}
