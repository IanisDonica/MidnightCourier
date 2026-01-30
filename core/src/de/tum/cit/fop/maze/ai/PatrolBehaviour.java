package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;

/**
 * AI behavior for patrolling between random tiles.
 */
public class PatrolBehaviour extends AIBehaviour {
    /** Minimum wait time at a patrol point. */
    private static final float MIN_WAIT = 0.5f;
    /** Maximum wait time at a patrol point. */
    private static final float MAX_WAIT = 2f;
    /** Current patrol target tile. */
    private GridPoint2 patrolTarget = null;
    /** Timer for waiting at a point. */
    private float waitTimer = 0f;
    /** Current randomized wait duration. */
    private float waitDuration = 0f;

    /**
     * Creates a patrol behavior.
     *
     * @param width map width in tiles
     * @param height map height in tiles
     * @param collisionLayer collision layer
     */
    public PatrolBehaviour(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    /**
     * Returns the current patrol target.
     *
     * @return patrol target or {@code null}
     */
    public GridPoint2 getPatrolTarget() {
        return patrolTarget;
    }

    /**
     * Starts patrolling to a random free tile.
     */
    public void startPatrol() {
        patrolTarget = findRandomFreeTile();
        waitTimer = 0f;
        waitDuration = 0f;
    }

    /**
     * Starts waiting with a random duration.
     */
    public void startWaiting() {
        waitTimer = 0f;
        waitDuration = MathUtils.random(MIN_WAIT, MAX_WAIT);
    }

    /**
     * Updates wait timer.
     *
     * @param delta frame delta time
     * @return {@code true} if wait time elapsed
     */
    public boolean updateWait(float delta) {
        waitTimer += delta;
        if (waitTimer >= waitDuration) {
            return true;
        }
        return false;
    }

    /**
     * Clears patrol state.
     */
    public void clear() {
        patrolTarget = null;
        waitTimer = 0f;
        waitDuration = 0f;
    }
}
