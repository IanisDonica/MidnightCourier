package de.tum.cit.fop.maze.entity.obstacle;

import de.tum.cit.fop.maze.system.PointManager;

public class Trap extends Obstacle {
    public Trap(float x, float y) {
        super(x, y, 1, 1, 0, 0, 1);
    }

    @Override
    public void collision() {
        if (player.isPotholeImmune()) {
            return;
        }
        if (!player.isStunned()) {
            player.damage(1);
        }
    }
}
