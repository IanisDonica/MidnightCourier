package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import de.tum.cit.fop.maze.entity.obstacle.Obstacle;

public class AIBehaviour {
    protected GridPoint2 findRandomFreeTile(int width, int height, Stage stage) {
        return findRandomFreeTileAvoiding(width, height, stage, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    protected GridPoint2 findRandomFreeTileAvoiding(int width, int height, Stage stage, int avoidX, int avoidY) {
        int maxAttempts = Math.max(16, width * height);
        for (int i = 0; i < maxAttempts; i++) {
            int x = MathUtils.random(0, width - 1);
            int y = MathUtils.random(0, height - 1);
            if (!isBlockedAt(x, y, stage) && !(x == avoidX && y == avoidY)) {
                return new GridPoint2(x, y);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isBlockedAt(x, y, stage) && !(x == avoidX && y == avoidY)) {
                    return new GridPoint2(x, y);
                }
            }
        }

        return new GridPoint2(0, 0);
    }

    protected boolean isBlockedAt(int x, int y, Stage stage) {
        if (stage == null) {
            return false;
        }
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Collectible || actor instanceof Obstacle) {
                int ax = MathUtils.floor(actor.getX());
                int ay = MathUtils.floor(actor.getY());
                if (ax == x && ay == y) {
                    return true;
                }
            }
        }
        return false;
    }
}
