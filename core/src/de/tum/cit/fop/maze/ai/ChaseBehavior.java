package de.tum.cit.fop.maze.ai;

public class ChaseBehavior extends AIBehaviour {
    private static final float MAX_CHASE_TIME = 5f;
    private float chaseTimer = 0f;

    public boolean shouldRetreat(float delta) {
        chaseTimer += delta;
        return chaseTimer >= MAX_CHASE_TIME;
    }

    public void reset() {
        chaseTimer = 0f;
    }
}
