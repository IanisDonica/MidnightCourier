package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class ChaseBehavior extends AIBehaviour {
    private static final float MAX_CHASE_TIME = 5f;
    private float chaseTimer = 0f;

    public ChaseBehavior(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    public boolean shouldRetreat(float delta) {
        chaseTimer += delta;
        return chaseTimer >= MAX_CHASE_TIME;
    }

    public void reset() {
        chaseTimer = 0f;
    }
}
