package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;

public class RetreatBehavior extends AIBehaviour {
    private static final float WAIT_DURATION = 3f;
    private GridPoint2 retreatTarget = null;
    private float waitTimer = 0f;

    public RetreatBehavior(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    public GridPoint2 getRetreatTarget() {
        return retreatTarget;
    }

    public void startRetreat() {
        retreatTarget = findRandomFreeTile();
        waitTimer = 0f;
    }

    public void startWaiting() {
        waitTimer = 0f;
    }

    public boolean updateWait(float delta) {
        waitTimer += delta;
        if (waitTimer >= WAIT_DURATION) {
            retreatTarget = null;
            return true;
        }
        return false;
    }
}
