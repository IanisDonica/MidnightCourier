package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;

/**
 * AI behavior for retreating to a random safe tile.
 */
public class RetreatBehavior extends AIBehaviour {
    /** Duration to wait while retreating. */
    private static final float WAIT_DURATION = 3f;
    /** Current retreat target tile. */
    private GridPoint2 retreatTarget = null;
    /** Timer for retreat waiting. */
    private float waitTimer = 0f;

    /**
     * Creates a retreat behavior.
     *
     * @param width map width in tiles
     * @param height map height in tiles
     * @param collisionLayer collision layer
     */
    public RetreatBehavior(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    /**
     * Returns the current retreat target.
     *
     * @return retreat target or {@code null}
     */
    public GridPoint2 getRetreatTarget() {
        return retreatTarget;
    }

    /**
     * Starts retreating to a random free tile.
     */
    public void startRetreat() {
        retreatTarget = findRandomFreeTile();
        waitTimer = 0f;
    }

    /**
     * Starts the retreat wait timer.
     */
    public void startWaiting() {
        waitTimer = 0f;
    }

    /**
     * Updates the wait timer and clears target when done.
     *
     * @param delta frame delta time
     * @return {@code true} if waiting finished
     */
    public boolean updateWait(float delta) {
        waitTimer += delta;
        if (waitTimer >= WAIT_DURATION) {
            retreatTarget = null;
            return true;
        }
        return false;
    }
}
