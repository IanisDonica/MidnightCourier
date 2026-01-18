package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.PointManager;

import java.util.concurrent.TimeUnit;

public class ExitDoor extends Collectible {
    // The door is obviously not a Collectible, but it's easier to implement it as such due to the colision code

    public ExitDoor(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        // Placeholder chest, should no animation be needed frame count can be set to 1
        initSpinAnimation(0, 0, 2);
    }

    @Override
    protected void collision() {
        if (this.player.hasKey()) {
            this.pointManager.saveScore(this.player.getHp());
            // if we exit imideatly the request wont get sent.
            try {
                TimeUnit.SECONDS.sleep(1); // wait 1s so the reques gets sent
            } catch (Exception ignored) {
            }
            throw new RuntimeException("You won :D");
        }
    }
}
