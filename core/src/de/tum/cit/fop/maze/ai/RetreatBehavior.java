package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class RetreatBehavior extends AIBehaviour {
    private static final float WAIT_DURATION = 3f;
    private GridPoint2 retreatTarget = null;
    private boolean waiting = false;
    private float waitTimer = 0f;

    public boolean isRetreating() {
        return retreatTarget != null;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public GridPoint2 getRetreatTarget() {
        return retreatTarget;
    }

    public void startRetreat(TiledMapTileLayer collisionLayer, Stage stage) {
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();
        retreatTarget = findRandomFreeTile(width, height, stage);
        waiting = false;
        waitTimer = 0f;
    }

    public void startWaiting() {
        waiting = true;
        waitTimer = 0f;
    }

    public boolean updateWait(float delta) {
        if (!waiting) {
            return false;
        }
        waitTimer += delta;
        if (waitTimer >= WAIT_DURATION) {
            waiting = false;
            retreatTarget = null;
            return true;
        }
        return false;
    }
}
