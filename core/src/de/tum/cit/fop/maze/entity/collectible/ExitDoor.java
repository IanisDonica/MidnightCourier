package de.tum.cit.fop.maze.entity.collectible;

import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.system.PointManager;

public class ExitDoor extends Collectible {
    // The door is obviously not a Collectible, but it's easier to implement it as such due to the colision code
    public interface VictoryListener {
        void onVictory();
    }

    private final VictoryListener victoryListener;
    private boolean triggered = false;

    public ExitDoor(float x, float y, PointManager pointManager, VictoryListener victoryListener) {
        super(x, y, 1, 1, pointManager);
        this.victoryListener = victoryListener;
        // Placeholder chest, should no animation be needed frame count can be set to 1
        initSpinAnimation(0, 0, 2);
    }

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
