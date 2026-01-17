package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;

public class RetreatBehavior extends AIBehaviour {
    private static final float WAIT_DURATION = 3f;
    private GridPoint2 retreatTarget = null;
    private boolean waiting = false;
    private float waitTimer = 0f;

    public RetreatBehavior(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    public boolean isRetreating() {
        return retreatTarget != null;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public GridPoint2 getRetreatTarget() {
        return retreatTarget;
    }

    public void startRetreat() {
        retreatTarget = findRandomFreeTile();
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
