package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class PatrolBehaviour extends AIBehaviour {
    private static final float MIN_WAIT = 0.5f;
    private static final float MAX_WAIT = 2f;
    private GridPoint2 patrolTarget = null;
    private boolean waiting = false;
    private float waitTimer = 0f;
    private float waitDuration = 0f;

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

    public void startPatrol(TiledMapTileLayer collisionLayer, Stage stage) {
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();
        patrolTarget = findRandomFreeTile(width, height, stage);
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
