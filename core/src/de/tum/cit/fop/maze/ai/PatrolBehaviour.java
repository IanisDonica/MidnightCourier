package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;

public class PatrolBehaviour extends AIBehaviour {
    private static final float MIN_WAIT = 10f;
    private static final float MAX_WAIT = 20f;
    private GridPoint2 patrolTarget = null;
    private boolean waiting = false;
    private float waitTimer = 0f;
    private float waitDuration = 0f;

    public PatrolBehaviour(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    public boolean isPatrolling() {
        return patrolTarget != null;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isActive() {
        return patrolTarget != null || waiting;
    }

    public GridPoint2 getPatrolTarget() {
        return patrolTarget;
    }

    public void startPatrol() {
        patrolTarget = findRandomFreeTile();
        waiting = false;
        waitTimer = 0f;
        waitDuration = 0f;
    }

    public void startWaiting() {
        waiting = true;
        waitTimer = 0f;
        waitDuration = MathUtils.random(MIN_WAIT, MAX_WAIT);
    }

    public boolean updateWait(float delta) {
        if (!waiting) {
            return false;
        }
        waitTimer += delta;
        if (waitTimer >= waitDuration) {
            waiting = false;
            return true;
        }
        return false;
    }

    public void clear() {
        patrolTarget = null;
        waiting = false;
        waitTimer = 0f;
        waitDuration = 0f;
    }
}
