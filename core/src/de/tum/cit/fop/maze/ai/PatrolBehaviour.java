package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;

public class PatrolBehaviour extends AIBehaviour {
    private static final float MIN_WAIT = 0.5f;
    private static final float MAX_WAIT = 2f;
    private GridPoint2 patrolTarget = null;
    private float waitTimer = 0f;
    private float waitDuration = 0f;

    public PatrolBehaviour(int width, int height, TiledMapTileLayer collisionLayer) {
        super(width, height, collisionLayer);
    }

    public GridPoint2 getPatrolTarget() {
        return patrolTarget;
    }

    public void startPatrol() {
        patrolTarget = findRandomFreeTile();
        waitTimer = 0f;
        waitDuration = 0f;
    }

    public void startWaiting() {
        waitTimer = 0f;
        waitDuration = MathUtils.random(MIN_WAIT, MAX_WAIT);
    }

    public boolean updateWait(float delta) {
        waitTimer += delta;
        if (waitTimer >= waitDuration) {
            return true;
        }
        return false;
    }

    public void clear() {
        patrolTarget = null;
        waitTimer = 0f;
        waitDuration = 0f;
    }
}
