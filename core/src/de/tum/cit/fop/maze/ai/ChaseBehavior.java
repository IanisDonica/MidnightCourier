package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * AI behavior for chasing the player and deciding when to retreat.
 */
public class ChaseBehavior extends AIBehaviour {
    /** Time without sight before retreating. */
    private static final float LOST_SIGHT_RETREAT_TIME = 2f;
    /** Timer tracking time since last sighting. */
    private float lostSightTimer = 0f;

    /**
     * Creates a chase behavior.
     *
     * @param width map width in tiles
     * @param height map height in tiles
     * @param collisionLayer collision layer
     */
    public ChaseBehavior(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    /**
     * Determines whether to retreat after losing sight of the player.
     *
     * @param canSeePlayer whether the player is visible
     * @param delta frame delta time
     * @return {@code true} if retreat should start
     */
    public boolean shouldRetreat(boolean canSeePlayer, float delta) {
        if (canSeePlayer) {
            lostSightTimer = 0f;
            return false;
        }
        lostSightTimer += delta;
        return lostSightTimer >= LOST_SIGHT_RETREAT_TIME;
    }

    /**
     * Resets the lost-sight timer.
     */
    public void reset() {
        lostSightTimer = 0f;
    }
}
