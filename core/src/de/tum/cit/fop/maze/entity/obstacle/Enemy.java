package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.ai.ChaseBehavior;
import de.tum.cit.fop.maze.ai.Pathfinder;
import de.tum.cit.fop.maze.ai.PatrolBehaviour;
import de.tum.cit.fop.maze.ai.RetreatBehavior;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import java.util.ArrayList;

public class Enemy extends Obstacle {
    private enum EnemyState {
        CHASING,
        PATROLLING,
        PATROL_WAIT,
        RETREATING,
        RETREAT_WAIT
    }

    private static final float PATH_RECALC_INTERVAL = 0.5f;
    private static final float TARGET_EPS = 0.05f;
    private static final float CENTER_EPS = 0.02f;
    private static final float PATROL_SPEED_SCALE = 0.5f;
    private static final int VISION_RANGE_TILES = 6;
    private final TiledMapTileLayer collisionLayer;
    private final int mapWidth;
    private final int mapHeight;
    private final float speed;
    private final Pathfinder pathfinder;
    private final ChaseBehavior chaseBehavior;
    private final RetreatBehavior retreatBehavior;
    private final PatrolBehaviour patrolBehavior;
    private static int globalRetreatToken = 0;
    private ArrayList<GridPoint2> path = new ArrayList<>();
    private int pathIndex = 0;
    private float pathRecalcTimer = 0f;
    private int lastGoalX = Integer.MIN_VALUE; // TODO Remove this
    private int lastGoalY = Integer.MIN_VALUE;
    private int lastRetreatToken = 0;
    private EnemyState state = EnemyState.CHASING; // Initial state

    public Enemy(TiledMapTileLayer collisionLayer, float x, float y) {
        super(x, y, 1,1, 0,0,3);
        this.collisionLayer = collisionLayer;
        this.pathfinder = new Pathfinder(collisionLayer);
        this.mapWidth = collisionLayer.getWidth();
        this.mapHeight = collisionLayer.getHeight();
        this.chaseBehavior = new ChaseBehavior(mapWidth, mapHeight, collisionLayer);
        this.retreatBehavior = new RetreatBehavior(mapWidth, mapHeight, collisionLayer);
        this.patrolBehavior = new PatrolBehaviour(mapWidth, mapHeight, collisionLayer);
        this.speed = 5f;
    }

    @Override
    protected void onAddedToStage() {
        super.onAddedToStage();
        Stage stage = getStage();
        chaseBehavior.setStage(stage);
        retreatBehavior.setStage(stage);
        patrolBehavior.setStage(stage);
        int[] coords = computePathCoords();
        path = pathfinder.findPath(coords[0], coords[1], coords[2], coords[3]);
        pathIndex = 0;
        ensureAboveCollectibles();
    }

    @Override
    public void act(float delta) {
        //TODO remove duplicate code
        super.act(delta);

        // If a global retreat has been called, only retreat if you are actually chasing the enemy.
        if (lastRetreatToken != globalRetreatToken) {
            lastRetreatToken = globalRetreatToken;
            if (state != EnemyState.RETREATING && state != EnemyState.RETREAT_WAIT) {
                enterRetreating();
            }
        }

        switch (state) {
            case RETREAT_WAIT:
                if (retreatBehavior.updateWait(delta)) {
                    chaseBehavior.reset();
                    enterPatrolling();
                }
                return;
            case PATROL_WAIT:
                if (patrolBehavior.updateWait(delta)) {
                    enterPatrolling();
                }
                return;
            case PATROLLING:
                if (canSeePlayer()) {
                    enterChasing();
                    break;
                }
                if (!path.isEmpty() && pathIndex >= path.size() && isCenteredOnTile()) {
                    enterPatrolWait();
                    return;
                }
                break;
            case CHASING:
                if (chaseBehavior.shouldRetreat(canSeePlayer(), delta)) {
                    enterRetreating();
                    return;
                }
                break;
            case RETREATING:
                if (!path.isEmpty() && pathIndex >= path.size() && isCenteredOnTile()) {
                    enterRetreatWait();
                    return;
                }
                break;
            default:
                break;
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

        followPath(delta);
    }

    private void followPath(float delta) {
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

        if (dist < TARGET_EPS) {
            pathIndex++;
            return;
        }

        float speedScale = (state == EnemyState.PATROLLING || state == EnemyState.RETREATING) ? PATROL_SPEED_SCALE : 1f;
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
        if (dist < TARGET_EPS) {
            setPosition(getX() + dx, getY() + dy);
            return;
        }
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    @Override
    protected void collision() {
        if (!player.isStunned() && state != EnemyState.RETREATING && state != EnemyState.RETREAT_WAIT) {
            player.damage(1);
            globalRetreatToken++; // causes all enemies to go into retreat
            lastRetreatToken = globalRetreatToken;
            enterRetreating();
        }
    }

    private int[] computePathCoords() {
        int startX = clampTileX(getX() + getWidth() / 2f);
        int startY = clampTileY(getY() + getHeight() / 2f);
        int goalX;
        int goalY;
        switch (state) {
            case RETREATING:
                GridPoint2 retreatTarget = retreatBehavior.getRetreatTarget();
                goalX = clampTileX(retreatTarget.x);
                goalY = clampTileY(retreatTarget.y);
                break;
            case PATROLLING:
                GridPoint2 patrolTarget = patrolBehavior.getPatrolTarget();
                goalX = clampTileX(patrolTarget.x);
                goalY = clampTileY(patrolTarget.y);
                break;
            default:
                goalX = clampTileX(player.getX() + player.getWidth() / 2f);
                goalY = clampTileY(player.getY() + player.getHeight() / 2f);
                break;
        }
        return new int[]{startX, startY, goalX, goalY};
    }

    private boolean isCenteredOnTile() {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float targetX = MathUtils.floor(centerX) + 0.5f;
        float targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;

        if (Math.abs(dx) < CENTER_EPS && Math.abs(dy) < CENTER_EPS) {
            setPosition(getX() + dx, getY() + dy);
            return true;
        }
        return false;
    }

    private boolean canSeePlayer() {
        int startX = clampTileX(getX() + getWidth() / 2f);
        int startY = clampTileY(getY() + getHeight() / 2f);
        int goalX = clampTileX(player.getX() + player.getWidth() / 2f);
        int goalY = clampTileY(player.getY() + player.getHeight() / 2f);

        if (Math.abs(goalX - startX) > VISION_RANGE_TILES || Math.abs(goalY - startY) > VISION_RANGE_TILES) {
            return false;
        }

        int dx = Math.abs(goalX - startX);
        int dy = Math.abs(goalY - startY);
        int sx = startX < goalX ? 1 : -1;
        int sy = startY < goalY ? 1 : -1;
        int err = dx - dy;
        int x = startX;
        int y = startY;

        while (true) {
            if (!isWalkable(x, y) && !(x == startX && y == startY)) {
                return false;
            }
            if (x == goalX && y == goalY) {
                return true;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return false;
        }
        return collisionLayer.getCell(x, y) == null;
    }

    private int clampTileX(float centerX) {
        return pathfinder.clampCoord(MathUtils.floor(centerX), mapWidth);
    }

    private int clampTileY(float centerY) {
        return pathfinder.clampCoord(MathUtils.floor(centerY), mapHeight);
    }

    private void enterRetreating() {
        state = EnemyState.RETREATING;
        patrolBehavior.clear();
        retreatBehavior.startRetreat();
        resetPathing();
        chaseBehavior.reset();
    }

    private void enterPatrolling() {
        state = EnemyState.PATROLLING;
        patrolBehavior.startPatrol();
        resetPathing();
    }

    private void enterPatrolWait() {
        state = EnemyState.PATROL_WAIT;
        patrolBehavior.startWaiting();
        pathRecalcTimer = 0f;
    }

    private void enterRetreatWait() {
        state = EnemyState.RETREAT_WAIT;
        retreatBehavior.startWaiting();
        pathRecalcTimer = 0f;
    }

    private void enterChasing() {
        state = EnemyState.CHASING;
        patrolBehavior.clear();
        resetPathing();
        chaseBehavior.reset();
    }

    private void resetPathing() {
        path.clear();
        pathIndex = 0;
        pathRecalcTimer = 0f;
        lastGoalX = Integer.MIN_VALUE;
        lastGoalY = Integer.MIN_VALUE;
    }

    //TODO see if this is needed
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
}
