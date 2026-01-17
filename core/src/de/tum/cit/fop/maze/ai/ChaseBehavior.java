package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class ChaseBehavior extends AIBehaviour {
    private static final float LOST_SIGHT_RETREAT_TIME = 2f;
    private float lostSightTimer = 0f;

    public ChaseBehavior(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    public boolean shouldRetreat(boolean canSeePlayer, float delta) {
        if (canSeePlayer) {
            lostSightTimer = 0f;
            return false;
        }
        lostSightTimer += delta;
        return lostSightTimer >= LOST_SIGHT_RETREAT_TIME;
    }

    public void reset() {
        lostSightTimer = 0f;
    }
}
