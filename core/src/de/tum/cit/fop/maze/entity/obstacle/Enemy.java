package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.ai.ChaseBehavior;
import de.tum.cit.fop.maze.ai.Pathfinder;
import de.tum.cit.fop.maze.ai.PatrolBehaviour;
import de.tum.cit.fop.maze.ai.RetreatBehavior;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import java.util.ArrayList;

public class Enemy extends Obstacle {
    private final TiledMapTileLayer collisionLayer;
    private final float speed;
    private final Pathfinder pathfinder;
    private final ChaseBehavior chaseBehavior;
    private final RetreatBehavior retreatBehavior;
    private final PatrolBehaviour patrolBehavior;
    private static int globalRetreatToken = 0;
    private ArrayList<GridPoint2> path = new ArrayList<>();
    private int pathIndex = 0;
    private float pathRecalcTimer = 0f;
    private static final float PATH_RECALC_INTERVAL = 0.5f;
    private int lastGoalX = Integer.MIN_VALUE;
    private int lastGoalY = Integer.MIN_VALUE;
    private int lastRetreatToken = 0;

    public Enemy(TiledMapTileLayer collisionLayer, float x, float y) {
        super(x, y, 1,1, 0,0,1);
        this.collisionLayer = collisionLayer;
        this.pathfinder = new Pathfinder(collisionLayer);
        this.chaseBehavior = new ChaseBehavior();
        this.retreatBehavior = new RetreatBehavior();
        this.patrolBehavior = new PatrolBehaviour();
        this.speed = 5f;
    }

    @Override
    protected void onAddedToStage() {
        super.onAddedToStage();
        int[] coords = computePathCoords();
        path = pathfinder.findPath(coords[0], coords[1], coords[2], coords[3]);
        pathIndex = 0;
        ensureAboveCollectibles();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (player != null) {
            if (lastRetreatToken != globalRetreatToken) {
                lastRetreatToken = globalRetreatToken;
                patrolBehavior.clear();
                retreatBehavior.startRetreat(collisionLayer, getStage());
                path.clear();
                pathIndex = 0;
                chaseBehavior.reset();
                pathRecalcTimer = 0f;
            }
            if (retreatBehavior.isWaiting()) {
                if (retreatBehavior.updateWait(delta)) {
                    chaseBehavior.reset();
                    pathRecalcTimer = 0f;
                    patrolBehavior.startPatrol(collisionLayer, getStage());
                    path.clear();
                    pathIndex = 0;
                }
                return;
            }

            if (patrolBehavior.isWaiting()) {
                if (patrolBehavior.updateWait(delta)) {
                    patrolBehavior.startPatrol(collisionLayer, getStage());
                    path.clear();
                    pathIndex = 0;
                    pathRecalcTimer = 0f;
                }
                return;
            }

            if (patrolBehavior.isPatrolling() && !path.isEmpty() && pathIndex >= path.size() && isCenteredOnTile()) {
                patrolBehavior.startWaiting();
                pathRecalcTimer = 0f;
                return;
            }

            if (!retreatBehavior.isRetreating() && !patrolBehavior.isActive() && chaseBehavior.shouldRetreat(delta)) {
                patrolBehavior.clear();
                retreatBehavior.startRetreat(collisionLayer, getStage());
                path.clear();
                pathIndex = 0;
                chaseBehavior.reset();
                pathRecalcTimer = 0f;
            }

            if (retreatBehavior.isRetreating() && pathIndex >= path.size() && isCenteredOnTile()) {
                retreatBehavior.startWaiting();
                pathRecalcTimer = 0f;
            }

            pathRecalcTimer -= delta;
            boolean pathExhausted = pathIndex >= path.size();
            if (pathRecalcTimer <= 0f && isCenteredOnTile()) {
                boolean needsRepath = path.isEmpty() || pathExhausted;
                int[] coords = computePathCoords();
                int goalX = coords[2];
                int goalY = coords[3];
                if (!needsRepath) {
                    boolean goalChanged = goalX != lastGoalX || goalY != lastGoalY;
                    needsRepath = goalChanged;
                }
                if (needsRepath) {
                    path = pathfinder.findPath(coords[0], coords[1], goalX, goalY);
                    pathIndex = 0;
                    lastGoalX = goalX;
                    lastGoalY = goalY;
                    pathRecalcTimer = PATH_RECALC_INTERVAL;
                }
            }
        }

        followPath(delta);
    }

    private void followPath(float delta) {
        //Sometimes the block doesnt end up on the center and as a result either gets stuck or crashes,
        //this is a simple way to prevent that
        if (pathIndex >= path.size()) {
            moveToTileCenter(delta);
            return;
        }
        GridPoint2 target = path.get(pathIndex);
        float targetX = target.x + 0.5f;
        float targetY = target.y + 0.5f;
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 0.05f) {
            pathIndex++;
            return;
        }

        float speedScale = patrolBehavior.isActive() ? 0.5f : 1f;
        float step = Math.min(speed * speedScale * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    private void moveToTileCenter(float delta) {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float targetX = MathUtils.floor(centerX) + 0.5f;
        float targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.05f) {
            setPosition(getX() + dx, getY() + dy);
            return;
        }
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    @Override
    protected void collision() {
        if (!player.isStunned() && !retreatBehavior.isRetreating()) {
            player.damage(1);
            triggerGlobalRetreat();
            lastRetreatToken = globalRetreatToken;
            patrolBehavior.clear();
            retreatBehavior.startRetreat(collisionLayer, getStage());
            path.clear();
            pathIndex = 0;
            chaseBehavior.reset();
            pathRecalcTimer = 0f;
        }
    }

    private int[] computePathCoords() {
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();

        //A* Node coordinate are integers, entity are floats, so we clamp them to int
        int startX = pathfinder.clampCoord(MathUtils.floor(getX() + getWidth() / 2f), width);
        int startY = pathfinder.clampCoord(MathUtils.floor(getY() + getHeight() / 2f), height);
        int goalX;
        int goalY;
        if (retreatBehavior.isRetreating()) {
            GridPoint2 target = retreatBehavior.getRetreatTarget();
            goalX = pathfinder.clampCoord(target.x, width);
            goalY = pathfinder.clampCoord(target.y, height);
        } else if (patrolBehavior.isPatrolling()) {
            GridPoint2 target = patrolBehavior.getPatrolTarget();
            goalX = pathfinder.clampCoord(target.x, width);
            goalY = pathfinder.clampCoord(target.y, height);
        } else {
            goalX = pathfinder.clampCoord(MathUtils.floor(player.getX() + player.getWidth() / 2f), width);
            goalY = pathfinder.clampCoord(MathUtils.floor(player.getY() + player.getHeight() / 2f), height);
        }
        return new int[]{startX, startY, goalX, goalY};
    }

    /*
       If the enemy decides to adjust path while not in the center of a tile, it tends to looks pretty wonky and jerky
       this check makes sure that the enemy only moves from tile to tile, not from half tile to half time,
       you can remove it and see how it looks without it if you wanna see why I added it.
     */
    private boolean isCenteredOnTile() {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float targetX = MathUtils.floor(centerX) + 0.5f;
        float targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;

        // If its 0.02f units away its close enough to the center
        float eps = 0.02f;
        if (Math.abs(dx) < eps && Math.abs(dy) < eps) {
            setPosition(getX() + dx, getY() + dy);
            return true;
        }
        return false;
    }


    private void ensureAboveCollectibles() {
        if (getStage() == null) {
            return;
        }
        int maxPickupZ = -1;
        for (int i = 0; i < getStage().getActors().size; i++) {
            if (getStage().getActors().get(i) instanceof Collectible) {
                int z = getStage().getActors().get(i).getZIndex();
                if (z > maxPickupZ) {
                    maxPickupZ = z;
                }
            }
        }
        if (maxPickupZ >= 0) {
            int targetZ = Math.min(maxPickupZ + 1, getStage().getActors().size - 1);
            if (player != null) {
                int playerZ = player.getZIndex();
                targetZ = Math.min(targetZ, Math.max(playerZ - 1, 0));
            }
            setZIndex(targetZ);
        }
    }

    private static void triggerGlobalRetreat() {
        globalRetreatToken++;
    }
}
